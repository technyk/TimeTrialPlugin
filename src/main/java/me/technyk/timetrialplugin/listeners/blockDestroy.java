package me.technyk.timetrialplugin.listeners;

import me.technyk.timetrialplugin.TimeTrialPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class blockDestroy implements Listener {

    @EventHandler
    public void onPlayerDestroy(BlockBreakEvent e){
        if(e.getBlock().getType().equals(Material.BLACK_BANNER) || e.getBlock().getType().equals(Material.BLUE_BANNER)){

            Block block = e.getBlock();
            BlockState blockState = block.getState();
            TileState tileState = (TileState) blockState;
            PersistentDataContainer container = tileState.getPersistentDataContainer();

            if(!(container.has(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttid"), PersistentDataType.INTEGER))) {
                return;
            }
            Integer ttid = container.get(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttid"), PersistentDataType.INTEGER);

            if(TimeTrialPlugin.getPlugin().getTtConfig().isConfigurationSection(String.valueOf(ttid))){
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "Nemůžeš zničit tento blok, jelikož obsahuje time trial!");
            }
            if(e.getPlayer().hasPermission("timetrial.admin")){
                e.getPlayer().sendMessage(ChatColor.RED + "Pro jehož odstranění proveď příkaz: " + ChatColor.DARK_RED + "/timetrial remove " + ttid);
            }

        }

    }

}
