package me.technyk.timetrialplugin.commands;

import me.technyk.timetrialplugin.TimeTrialPlugin;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.Rotatable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class timetrialCommand implements CommandExecutor {

    private final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    private BlockFace yawToFace(float yaw) {
        return radial[Math.round(yaw / 45f) & 0x7];
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is sent by a player
        if(!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player");
            return true;
        }

        // Cast the sender to a player
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte" + ChatColor.DARK_RED + " /timetrial help" + ChatColor.RED + " pro více informací");
            return true;
        }

        // Make a switch statement to check the command arguments
        switch(args[0]) {
            case "help":
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "--- TimeTrialPlugin Help ---");
                player.sendMessage(ChatColor.GOLD + "/timetrial help " + ChatColor.YELLOW + "- Zobrazí pomoc s příkazy");
                player.sendMessage(ChatColor.GOLD + "/timetrial create <název> " + ChatColor.YELLOW + "- Vytvoří nový time trial tam kde stojíš");
                player.sendMessage(ChatColor.GOLD + "/timetrial set <id> end " + ChatColor.YELLOW + "- Nastaví konec time trialu tam kde stojíš");
                player.sendMessage(ChatColor.GOLD + "/timetrial set <id> time <sekundy>" + ChatColor.YELLOW + "- Nastaví limit pro dokončení time trialu");
                player.sendMessage(ChatColor.GOLD + "/timetrial set <id> info <text>" + ChatColor.YELLOW + "- Nastaví info pro time trial");
                player.sendMessage(ChatColor.GOLD + "/timetrial set <id> reward add <příkaz>" + ChatColor.YELLOW + "- Přidá příkaz jako odměnu za dokončení time trialu (%plr% pro jméno hráče)");
                player.sendMessage(ChatColor.GOLD + "/timetrial set <id> reward list" + ChatColor.YELLOW + "- Vypíše veškeré odměnové příkazy time trialu a jejich id");
                player.sendMessage(ChatColor.GOLD + "/timetrial set <id> reward remove <id příkazu>" + ChatColor.YELLOW + "- Odebere odměnový příkaz");
                player.sendMessage(ChatColor.GOLD + "/timetrial list" + ChatColor.YELLOW + "- Vypíše všechny time trialy a jejich pozice");
                player.sendMessage(ChatColor.GOLD + "/timetrial remove <id>" + ChatColor.YELLOW + "- Odebere time trial");
                player.sendMessage(ChatColor.GOLD + "/timetrial refresh" + ChatColor.YELLOW + "- Opraví všechny time trialy");



            case "create":

                // Check if the player has the correct amount of arguments
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial create <název>");
                    return true;
                }

                // get player's location
                Location loc = player.getLocation();
                Block block = loc.getBlock();

                // check if the block is available (nothing is there to be replaced)
                if(!block.isEmpty()){
                    player.sendMessage(ChatColor.RED + "Není zde dostatek místa pro umištění začátku time trialu!");
                    return true;
                }

                // create a block at the player's location
                block.setType(Material.BLACK_BANNER);
                Rotatable rotatable = (Rotatable) block.getBlockData();
                Banner banner = (Banner) block.getState();
                banner.addPattern(new Pattern(DyeColor.WHITE, PatternType.GLOBE));

                // convert the player's yaw to a direction and set it
                float plrYaw = player.getLocation().getYaw();
                BlockFace bf = yawToFace(plrYaw);
                rotatable.setRotation(bf);
                banner.setBlockData(rotatable);
                banner.update();

                // add a persistent data container
                BlockState blockState = block.getState();

                TileState tileState = (TileState) blockState;

                PersistentDataContainer container = tileState.getPersistentDataContainer();


                Integer lastId = TimeTrialPlugin.getPlugin().getLastId();

                if(lastId != -1){
                    TimeTrialPlugin.getPlugin().getTtConfig().set("lastId", ++lastId);
                    player.sendMessage(ChatColor.YELLOW + "Vytvořen nový time trial s ID: " + ChatColor.GOLD + lastId + ChatColor.YELLOW + "\nPoužijte příkaz " + ChatColor.GOLD + "/timetrial set " + lastId + " (end/time/info/reward)" + ChatColor.YELLOW + " pro nastavení time trialu");
                    try {
                        TimeTrialPlugin.getPlugin().getTtConfig().save(TimeTrialPlugin.getPlugin().getTtConfigFile());
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                    container.set(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttid"), PersistentDataType.INTEGER, lastId);
                    container.set(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttdir"), PersistentDataType.STRING, "start");
                    tileState.update();

                    StringBuilder sb = new StringBuilder();
                    for(int i = 1; i < args.length; i++){
                        if(i>1) sb.append(" ");
                        sb.append(args[i]);
                    }
                    TimeTrialPlugin.getPlugin().getTtConfig().set(lastId + ".Name", sb.toString());

                    ArmorStand StartHologram = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5,2,0.5), EntityType.ARMOR_STAND);

                    StartHologram.setVisible(false);
                    StartHologram.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + sb);
                    StartHologram.setCustomNameVisible(true);
                    StartHologram.setGravity(false);
                    StartHologram.setCollidable(false);
                    StartHologram.setInvulnerable(true);
                    StartHologram.setMarker(true);
                    StartHologram.addScoreboardTag("Start" + lastId);

                    ArmorStand StartHologram2 = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5,1.75,0.5), EntityType.ARMOR_STAND);

                    StartHologram2.setVisible(false);
                    StartHologram2.setCustomName(ChatColor.GOLD + "(Start)");
                    StartHologram2.setCustomNameVisible(true);
                    StartHologram2.setGravity(false);
                    StartHologram2.setCollidable(false);
                    StartHologram2.setInvulnerable(true);
                    StartHologram2.setMarker(true);
                    StartHologram2.addScoreboardTag("Start" + lastId);

                    TimeTrialPlugin.getPlugin().getTtConfig().set(lastId + ".Time", "90");
                    TimeTrialPlugin.getPlugin().getTtConfig().set(lastId + ".Info", "None");
                    List<String> cmdList = Collections.emptyList();
                    TimeTrialPlugin.getPlugin().getTtConfig().set(lastId + ".Commands", cmdList);
                    TimeTrialPlugin.getPlugin().getTtConfig().set(lastId + ".StartLocation", TimeTrialPlugin.getPlugin().loc2str(loc));
                    TimeTrialPlugin.getPlugin().getTtConfig().set(lastId + ".EndLocation", "None");

                    try {
                        TimeTrialPlugin.getPlugin().getTtConfig().save(TimeTrialPlugin.getPlugin().getTtConfigFile());
                    } catch (IOException err) {
                        err.printStackTrace();
                    }


                }else{
                    System.out.println("[TimeTrialPlugin] ttStorage.yml doesn't exist! Please report this issue to the developer of the plugin!");
                }
                return true;

            case "set":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial set <id> <end/time/info/reward> [hodnota]");
                    return true;
                }
                if(!(TimeTrialPlugin.getPlugin().getTtConfig().isConfigurationSection(args[1]))) {
                    player.sendMessage(ChatColor.RED + "Zadané ID time trialu neexistuje!");
                    return true;
                }

                switch(args[2]){
                    case "info":
                        if (args.length < 4) {
                            player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial set " +args[1]+" info <hodnota>");
                            return true;
                        }
                        StringBuilder sb2 = new StringBuilder();
                        for(int i = 3; i < args.length; i++){
                            if(i>3) sb2.append(" ");
                            sb2.append(args[i]);
                        }
                        TimeTrialPlugin.getPlugin().getTtConfig().set(args[1] + ".Info", sb2.toString());
                        try {
                            TimeTrialPlugin.getPlugin().getTtConfig().save(TimeTrialPlugin.getPlugin().getTtConfigFile());
                        } catch (IOException err) {
                            err.printStackTrace();
                        }
                        player.sendMessage(ChatColor.YELLOW + "Hodnota " + ChatColor.GOLD + "info" + ChatColor.YELLOW + " time trialu s ID " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + " byla nastavena na '" + ChatColor.GOLD + sb2 + ChatColor.YELLOW + "'.");
                        return true;
                    case "time":
                        if (args.length != 4) {
                            player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial set " +args[1]+" time <hodnota (v sekundách)>");
                            return true;
                        }
                        try {
                            Integer.parseInt(args[3]);
                        }catch(NumberFormatException e){
                            player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Při zadávání hodnoty " + ChatColor.DARK_RED + "time" + ChatColor.RED + " použijte čas v " + ChatColor.DARK_RED + "sekundách" + ChatColor.RED + "!");
                            return true;
                        }
                        TimeTrialPlugin.getPlugin().getTtConfig().set(args[1] + ".Time", args[3]);
                        try {
                            TimeTrialPlugin.getPlugin().getTtConfig().save(TimeTrialPlugin.getPlugin().getTtConfigFile());
                        } catch (IOException err) {
                            err.printStackTrace();
                        }
                        player.sendMessage(ChatColor.YELLOW + "Hodnota " + ChatColor.GOLD + "time" + ChatColor.YELLOW + " time trialu s ID " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + " byla nastavena na '" + ChatColor.GOLD + args[3] + ChatColor.YELLOW + "'.");
                        return true;
                    case "end":

                        // get player's location
                        Location locend = player.getLocation();
                        Block blockend = locend.getBlock();

                        // check if the block is available (nothing is there to be replaced)
                        if(!blockend.isEmpty()){
                            player.sendMessage(ChatColor.RED + "Není zde dostatek místa pro umištění konce time trialu!");
                            return true;
                        }

                        // create a block at the player's location
                        blockend.setType(Material.BLUE_BANNER);
                        Rotatable rotatableend = (Rotatable) blockend.getBlockData();
                        Banner bannerend = (Banner) blockend.getState();
                        bannerend.addPattern(new Pattern(DyeColor.WHITE, PatternType.GLOBE));

                        // convert the player's yaw to a direction and set it
                        float plrYaw2 = player.getLocation().getYaw();
                        BlockFace bf2 = yawToFace(plrYaw2);
                        rotatableend.setRotation(bf2);
                        bannerend.setBlockData(rotatableend);
                        bannerend.update();

                        // add a persistent data container
                        BlockState blockStateend = blockend.getState();

                        TileState tileStateend = (TileState) blockStateend;

                        PersistentDataContainer containerend = tileStateend.getPersistentDataContainer();

                        player.sendMessage(ChatColor.YELLOW + "Vytvořen nový cíl pro time trial s ID: " + ChatColor.GOLD + args[1]);
                        containerend.set(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttid"), PersistentDataType.INTEGER, Integer.parseInt(args[1]));
                        containerend.set(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttdir"), PersistentDataType.STRING, "end");
                        tileStateend.update();

                        if(!(TimeTrialPlugin.getPlugin().getTtConfig().getString(args[1] + ".EndLocation").equals("None"))) {
                            String locToParse = TimeTrialPlugin.getPlugin().getTtConfig().getString(args[1] + ".EndLocation");
                            Location locToRemove = TimeTrialPlugin.getPlugin().str2loc(locToParse);
                            Block blockToRemove = locToRemove.getBlock();
                            blockToRemove.setType(Material.AIR);

                            for(World w : Bukkit.getWorlds()){
                                for(Entity e : w.getEntities()) {
                                    if (e.getType().equals(EntityType.ARMOR_STAND)){
                                        for (String s : e.getScoreboardTags()) {
                                            if ((s.startsWith("End")) && (s.substring(3).equals(args[1])) ) {
                                                e.remove();
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ArmorStand EndHologram = (ArmorStand) blockend.getWorld().spawnEntity(blockend.getLocation().add(0.5,2,0.5), EntityType.ARMOR_STAND);

                        EndHologram.setVisible(false);
                        EndHologram.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + TimeTrialPlugin.getPlugin().getTtConfig().getString(args[1] + ".Name"));
                        EndHologram.setCustomNameVisible(true);
                        EndHologram.setGravity(false);
                        EndHologram.setCollidable(false);
                        EndHologram.setInvulnerable(true);
                        EndHologram.setMarker(true);
                        EndHologram.addScoreboardTag("End" + args[1]);

                        ArmorStand EndHologram2 = (ArmorStand) blockend.getWorld().spawnEntity(blockend.getLocation().add(0.5,1.75,0.5), EntityType.ARMOR_STAND);

                        EndHologram2.setVisible(false);
                        EndHologram2.setCustomName(ChatColor.GOLD + "(Konec)");
                        EndHologram2.setCustomNameVisible(true);
                        EndHologram2.setGravity(false);
                        EndHologram2.setCollidable(false);
                        EndHologram2.setInvulnerable(true);
                        EndHologram2.setMarker(true);
                        EndHologram2.addScoreboardTag("End" + args[1]);

                        TimeTrialPlugin.getPlugin().getTtConfig().set(args[1] + ".EndLocation", TimeTrialPlugin.getPlugin().loc2str(locend));

                        try {
                            TimeTrialPlugin.getPlugin().getTtConfig().save(TimeTrialPlugin.getPlugin().getTtConfigFile());
                        } catch (IOException err) {
                            err.printStackTrace();
                        }


                        return true;
                    case "reward":
                        if (args.length < 4) {
                            player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial set " +args[1]+" reward <add/remove/list> [hodnota]");
                            return true;
                        }
                        switch (args[3]) {
                            // Mám tu víc proměnných se jménem CmdList protože mi intellij házel nějaké chyby
                            case "list":
                                if (args.length != 4) {
                                    player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial set " + args[1] + " reward list");
                                }
                                List<String> CmdList = TimeTrialPlugin.getPlugin().getTtConfig().getStringList(args[1] + ".Commands");
                                for (int z = 0; z < CmdList.size(); z++) {
                                    CmdList.set(z, ChatColor.GOLD + "" + z + ": " + ChatColor.WHITE + CmdList.get(z));
                                }
                                player.sendMessage(ChatColor.YELLOW + "Odměnové příkazy time trialu " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + ":");
                                player.sendMessage(String.join("\n", CmdList));
                                return true;
                            case "add":
                                if (args.length < 5) {
                                    player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial set " + args[1] + " reward add [příkaz]");
                                }
                                StringBuilder sb3 = new StringBuilder();
                                for (int i = 4; i < args.length; i++) {
                                    if (i > 4) sb3.append(" ");
                                    sb3.append(args[i]);
                                }
                                List<String> CmdList2 = TimeTrialPlugin.getPlugin().getTtConfig().getStringList(args[1] + ".Commands");
                                CmdList2.add(String.valueOf(sb3));
                                TimeTrialPlugin.getPlugin().getTtConfig().set(args[1] + ".Commands", CmdList2);
                                try {
                                    TimeTrialPlugin.getPlugin().getTtConfig().save(TimeTrialPlugin.getPlugin().getTtConfigFile());
                                } catch (IOException err) {
                                    err.printStackTrace();
                                    return true;
                                }
                                player.sendMessage(ChatColor.YELLOW + "Odměnový příkaz '" + ChatColor.GOLD + sb3 + ChatColor.YELLOW + "' byl přidán do time trialu s id " + ChatColor.GOLD + args[1]);
                                return true;
                            case "remove":
                                if (args.length != 5) {
                                    player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial set " + args[1] + " reward remove [číslo]");
                                }
                                try {
                                    Integer.parseInt(args[4]);
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial set " + args[1] + " reward remove [číslo]");
                                    return true;
                                }
                                List<String> CmdList3 = TimeTrialPlugin.getPlugin().getTtConfig().getStringList(args[1] + ".Commands");
                                if(CmdList3.get(Integer.parseInt(args[4])) != null){
                                    CmdList3.remove(Integer.parseInt(args[4]));
                                    player.sendMessage(ChatColor.YELLOW + "Odměnový příkaz s ID " + ChatColor.GOLD + args[4] + ChatColor.YELLOW + " byl úspěšně odstraněn");
                                    player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "VAROVÁNÍ: " + ChatColor.YELLOW + "Pořádí příkazů (společně s jejich ID) v time trialu se změnilo!");
                                }else{
                                    player.sendMessage(ChatColor.RED + "Zadané ID příkazu neexistuje! Použijte" +ChatColor.DARK_RED+ " /timetrial set " + args[1] + " reward list" + ChatColor.RED + "pro zobrazení ID všech příkazů");
                                }
                                TimeTrialPlugin.getPlugin().getTtConfig().set(args[1] + ".Commands", CmdList3);
                                try {
                                    TimeTrialPlugin.getPlugin().getTtConfig().save(TimeTrialPlugin.getPlugin().getTtConfigFile());
                                } catch (IOException err) {
                                    err.printStackTrace();
                                }
                                return true;
                        }
//                        player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial set " +args[1]+" reward <add/remove/list> [hodnota]");
                        return true;
                }
//                player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte:" + ChatColor.DARK_RED + " /timetrial set <id> <end/time/info/reward> [hodnota]");
                return true;
            case "list":
                player.sendMessage(ChatColor.YELLOW + "Listina všech time trialů:");
                player.sendMessage(ChatColor.DARK_GRAY + "Přejeďte myší nad ID pro více informací");

                for(int x = 0; x < (TimeTrialPlugin.getPlugin().getLastId() + 1); x++){
                    if(TimeTrialPlugin.getPlugin().getTtConfig().isConfigurationSection(String.valueOf(x))){
                        TextComponent message = new TextComponent(ChatColor.GRAY + "- " + ChatColor.GOLD + "" + x + ChatColor.GRAY + " | " + ChatColor.GOLD + (TimeTrialPlugin.getPlugin().getTtConfig().getString(x + ".Name")));
                        message.setFont("minecraft:default");
                        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                                ChatColor.GOLD + "Jméno: " + ChatColor.WHITE + (TimeTrialPlugin.getPlugin().getTtConfig().getString(x + ".Name")) + "\n" +
                                        ChatColor.GOLD + "ID: " + ChatColor.WHITE + x + "\n" +
                                        ChatColor.GOLD + "Čas: " + ChatColor.WHITE + (TimeTrialPlugin.getPlugin().getTtConfig().getString(x + ".Time")) + "\n" +
                                        ChatColor.GOLD + "Start: " + ChatColor.WHITE + (TimeTrialPlugin.getPlugin().getTtConfig().getString(x + ".StartLocation")) + "\n" +
                                        ChatColor.GOLD + "Konec: " + ChatColor.WHITE + (TimeTrialPlugin.getPlugin().getTtConfig().getString(x + ".EndLocation")) + "\n" +
                                        ChatColor.GOLD + "Info: " + ChatColor.WHITE + (TimeTrialPlugin.getPlugin().getTtConfig().getString(x + ".Info"))
                        )));
                        player.spigot().sendMessage(message);
                    }
                }
                return true;
            case "delete":
            case "remove":
                if(args.length < 2){
                    player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte" + ChatColor.DARK_RED + " /timetrial remove <id>" + ChatColor.RED + " pro odstranění time trialu");
                    return true;
                }
                if(args.length < 3){
                    if(TimeTrialPlugin.getPlugin().getTtConfig().isConfigurationSection(args[1])) {
                        player.sendMessage(ChatColor.YELLOW + "Jste si opravdu jistý že chcete odstranit time trial s ID " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "?\nTímto smažete veškeré jeho nastavení, cíl i start");
                        player.sendMessage(ChatColor.YELLOW+ "Pro pokračování proveďte příkaz " + ChatColor.GOLD + "/timetrial remove " + args[1] + " confirm");
                    }else{
                        player.sendMessage(ChatColor.RED + "Zadané ID time trialu neexistuje!");
                    }
                    return true;
                }else {
                    if (TimeTrialPlugin.getPlugin().getTtConfig().isConfigurationSection(args[1])) {
                        String StartString = TimeTrialPlugin.getPlugin().getTtConfig().getString(args[1] + ".StartLocation");
                        if (StartString != null) {
                            Location StartBlockLoc = TimeTrialPlugin.getPlugin().str2loc(StartString);
                            Block StartBlock = StartBlockLoc.getBlock();
                            StartBlock.setType(Material.AIR);

                            for (Entity e : StartBlockLoc.getWorld().getEntities()) {
                                if (e.getType().equals(EntityType.ARMOR_STAND)) {
                                    for (String s : e.getScoreboardTags()) {
                                        if ((s.startsWith("Start")) && (s.substring(5).equals(String.valueOf(args[1])))) {
                                            e.remove();
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        String EndString = TimeTrialPlugin.getPlugin().getTtConfig().getString(args[1] + ".EndLocation");
                        if (EndString != null && !(EndString.equals("None"))) {
                            Location EndBlockLoc = TimeTrialPlugin.getPlugin().str2loc(EndString);
                            Block EndBlock = EndBlockLoc.getBlock();
                            EndBlock.setType(Material.AIR);

                            for (Entity e : EndBlockLoc.getWorld().getEntities()) {
                                if (e.getType().equals(EntityType.ARMOR_STAND)) {
                                    for (String s : e.getScoreboardTags()) {
                                        if ((s.startsWith("End")) && (s.substring(3).equals(String.valueOf(args[1])))) {
                                            e.remove();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        TimeTrialPlugin.getPlugin().getTtConfig().set(args[1], null);
                        try {
                            TimeTrialPlugin.getPlugin().getTtConfig().save(TimeTrialPlugin.getPlugin().getTtConfigFile());
                        } catch (IOException err) {
                            err.printStackTrace();
                        }
                        player.sendMessage(ChatColor.YELLOW + "Time trial s ID " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + " byl úspěšně odstraněn");
                    }else{
                        player.sendMessage(ChatColor.RED + "Zadané ID time trialu neexistuje!");
                    }
                }
                return true;
            case "refresh":
                player.sendMessage(ChatColor.GOLD + "Bylo spuštěno obnovení time trialů. Vyčkejte prosím.");
                ArrayList<String> foundProblems = TimeTrialPlugin.getPlugin().refreshMarkers();
                player.sendMessage(ChatColor.YELLOW + "-*-*-*-*-*-*-*-*-");
                player.sendMessage(ChatColor.GOLD + "Proces obnovování byl dokončen!");
                player.sendMessage(ChatColor.GOLD + "Nalezeno a opraveno: " + ChatColor.WHITE + foundProblems.get(0));
                player.sendMessage(ChatColor.GOLD + "\nNeopravitelné chyby: ");
                if(foundProblems.size() > 1) {
                    for (int i = 1; i < foundProblems.size(); i++) {
                        player.sendMessage(ChatColor.RED + "Překážejicí blok! " + ChatColor.DARK_RED + "(" + foundProblems.get(i) + ")");
                    }
                }else{
                    player.sendMessage(ChatColor.WHITE + "Žádné!");
                }
                player.sendMessage(ChatColor.YELLOW + "-*-*-*-*-*-*-*-*-");
                return true;
        }
        player.sendMessage(ChatColor.RED + "Nesprávný příkaz! Použijte" + ChatColor.DARK_RED + " /timetrial help" + ChatColor.RED + " pro více informací");
        return true;
    }
}
