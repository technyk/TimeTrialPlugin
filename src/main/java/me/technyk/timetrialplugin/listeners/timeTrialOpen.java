package me.technyk.timetrialplugin.listeners;

import me.technyk.timetrialplugin.TimeTrialPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;



public class timeTrialOpen implements Listener {

    // Make a list consisting integers 1,2,3,4,5,10,20 + every multiplication of 30 till 600
    private final Integer[] timeAnnounce = {1, 2, 3, 4, 5, 10, 20, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330, 360, 390, 420, 450, 480, 510, 540, 570, 600};
    public final static HashMap<UUID, Integer> currentTimeTrial = new HashMap<>();
    public final static HashMap<UUID, Integer> currentTime = new HashMap<>();

    public void startTimer(Plugin plugin) {
        new BukkitRunnable() {

            @Override
            public void run() {

                for (UUID uuid : currentTimeTrial.keySet()) {

                    Integer plrTime = currentTime.get(uuid);

                    if (currentTime.get(uuid) != null) {


                        Player player = Bukkit.getPlayer(uuid);

                        if (player == null || !(player.isOnline())) {

                            // remove the player from both hashmaps
                            currentTimeTrial.remove(uuid);
                            currentTime.remove(uuid);
                            return;
                        }

                        if(!(TimeTrialPlugin.getPlugin().getTtConfig().isConfigurationSection(String.valueOf(currentTimeTrial.get(uuid))))){
                            currentTimeTrial.remove(uuid);
                            currentTime.remove(uuid);
                            player.sendMessage(ChatColor.YELLOW + "Time trial ve kterém jsi byl zapojen byl smazán.");
                        }

                        if (plrTime > 0) {

                            plrTime -= 1;
                            currentTime.put(uuid, plrTime);

                            // check if the current time is in the list timeAnnounce
                            if (Arrays.asList(timeAnnounce).contains(plrTime)) {

                                player.sendMessage(ChatColor.YELLOW + "Již zbývá jen " + ChatColor.GOLD + plrTime + "s" + ChatColor.YELLOW + " do konce probíhajicího time trialu!");

                            }

                        } else {
                            // remove the player from the HashMap
                            currentTimeTrial.remove(uuid);
                            currentTime.remove(uuid);
                            player.sendMessage(ChatColor.RED + "Nestihl jsi dokončit time trial v čas!");
                        }

                    }

                }

            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void startTimeTrial(Player plr, Integer id, Integer length){
        Location EndLocation = TimeTrialPlugin.getPlugin().str2loc(TimeTrialPlugin.getPlugin().getTtConfig().getString(id + ".EndLocation"));

        plr.sendMessage(ChatColor.YELLOW + "Time trial začal! Dostav se na souřadnice níže!");
        plr.sendMessage(ChatColor.YELLOW + "X: " + ChatColor.GOLD + EndLocation.getX() + ChatColor.YELLOW + " Y: " + ChatColor.GOLD + EndLocation.getY() + ChatColor.YELLOW + " Z: " + ChatColor.GOLD + EndLocation.getZ());

        currentTime.put(plr.getUniqueId(), length);
        currentTimeTrial.put(plr.getUniqueId(), id);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e){
        Player player = e.getPlayer();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null || e.getHand() == EquipmentSlot.HAND) return;
        if(e.getClickedBlock().getType() != Material.BLACK_BANNER && e.getClickedBlock().getType() != Material.BLUE_BANNER) return;

        Block block = e.getClickedBlock();
        BlockState blockState = block.getState();
        TileState tileState = (TileState) blockState;
        PersistentDataContainer container = tileState.getPersistentDataContainer();

        if(!(container.has(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttid"), PersistentDataType.INTEGER))) return;
        Integer ttid = container.get(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttid"), PersistentDataType.INTEGER);
        if(!(container.has(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttdir"), PersistentDataType.STRING))) return;
        String direction = container.get(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttdir"), PersistentDataType.STRING);
        if(direction == null) return; // Toto tu je aby intellij neřval. Nevím proč mu to vadí u tohohle a ne u ttid

        if (e.getClickedBlock().getType().equals(Material.BLACK_BANNER)){

            if(direction.equals("end")) {
                player.sendMessage(ChatColor.RED + "Při spouštění time trialu nastala chyba! Kontaktujte administrátora severu o tomto problému!");
                System.out.println("[TimeTrialPlugin] Time trial with the id " + ttid + " has a wrong block at it's start");
                return;
            }

            if(currentTimeTrial.containsKey(player.getUniqueId())){
                player.sendMessage(ChatColor.RED + "Již se účastníš time trialu '" + ChatColor.DARK_RED + TimeTrialPlugin.getPlugin().getTtConfig().getString(currentTimeTrial.get(player.getUniqueId()) + ".Name") + " (" + currentTimeTrial.get(player.getUniqueId()) + ")" + ChatColor.RED + "'!");
                return;
            }

            String unparsedLocation = TimeTrialPlugin.getPlugin().getTtConfig().getString(ttid + ".EndLocation");
            if(unparsedLocation == null || unparsedLocation.equals("None")){
                player.sendMessage(ChatColor.RED + "U tohoto time trialu ještě není nastaven cíl!");
                return;
            }

            String unparsedlength = TimeTrialPlugin.getPlugin().getTtConfig().getString(ttid + ".Time");
            if(unparsedlength == null) return;
            int length = Integer.parseInt(unparsedlength);

            // Make a GUI with 9 slots
            Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.BLACK + "" + ChatColor.BOLD + "Time Trial " + ChatColor.BLACK + ttid);

            ItemStack startItem = new ItemStack(Material.EMERALD);
            ItemMeta startMeta = startItem.getItemMeta();
            startMeta.setDisplayName(ChatColor.GREEN + "Začít time trial");
            startMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Po kliknutí na tento item se spustí time trial"));

            ItemStack infoItem = new ItemStack(Material.BOOK);
            ItemMeta infoMeta = infoItem.getItemMeta();
            infoMeta.setDisplayName(ChatColor.GOLD + "Info");
            ArrayList<String> loreList = new ArrayList<>();

            List<String> commandList = TimeTrialPlugin.getPlugin().getTtConfig().getStringList(ttid + ".Commands");

            if(commandList.size() > 0) {
                loreList.add(ChatColor.YELLOW + "Odměna: " + ChatColor.GREEN + "Ano");
            }else{
                loreList.add(ChatColor.YELLOW + "Odměna: " + ChatColor.RED + "Ne");
            }

            loreList.add(ChatColor.YELLOW + "Limit: " + ChatColor.WHITE + length + "s");
            loreList.add(ChatColor.YELLOW + "Info: ");
            loreList.add(ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', TimeTrialPlugin.getPlugin().getTtConfig().getString(ttid + ".Info", "Nic")));
            infoMeta.setLore(loreList);

            startItem.setItemMeta(startMeta);
            infoItem.setItemMeta(infoMeta);

            inventory.setItem(3, startItem);
            inventory.setItem(5, infoItem);

            player.openInventory(inventory);

        }else if(e.getClickedBlock().getType().equals(Material.BLUE_BANNER)){

            if(direction.equals("start")) {
                player.sendMessage(ChatColor.RED + "Při spouštění time trialu nastala chyba! Kontaktujte administrátora severu o tomto problému!");
                System.out.println("[TimeTrialPlugin] Time trial with the id " + ttid + " has a wrong block at it's end");
                return;
            }

            if(currentTimeTrial.containsKey(player.getUniqueId())){

                if(currentTime.get(player.getUniqueId()) <= 0) return;

                // remove the player from both hashmaps
                currentTimeTrial.remove(player.getUniqueId());
                currentTime.remove(player.getUniqueId());

                List<String> commandList = TimeTrialPlugin.getPlugin().getTtConfig().getStringList(ttid + ".Commands");

                if(commandList.size() > 0){
                    for(String command : commandList){

                        command = command.replace("%plr%", player.getName());

                        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                        Bukkit.dispatchCommand(console, command);
                        System.out.println("[TimeTrialPlugin] Gave rewards to " + player.getName() + " for completing time trial with id " + ttid);

                    }
                    player.sendMessage(ChatColor.YELLOW + "Byla ti udělena odměna za splnění time trialu.");
                }else{
                    player.sendMessage(ChatColor.YELLOW + "U tohoto time trialu není nastavena žádná odměna.");
                }

            }

        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e){
        String title = ChatColor.stripColor(e.getView().getTitle());
        if(e.getClickedInventory() == null) return;
        if(title.startsWith("Time Trial ") && e.getClickedInventory().getSize() == 9 && e.getClickedInventory().getHolder() == e.getWhoClicked()){
            Player plr = (Player) e.getWhoClicked();
            Integer id = Integer.valueOf(title.substring(11));
            e.setCancelled(true);
            if(e.getCurrentItem().getType().equals(Material.EMERALD)){
                startTimeTrial(plr, id, Integer.valueOf(TimeTrialPlugin.getPlugin().getTtConfig().getString(id + ".Time")));
                plr.closeInventory();
            }
        }
    }
}
