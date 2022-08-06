package me.technyk.timetrialplugin.listeners;

import me.technyk.timetrialplugin.TimeTrialPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class entityDamage implements Listener {

    /*
    * Tenhle listener je tu jenom pro ujištění, že armorstand opravdu nemůže být zničený hráčem (či jinou entitou)
    * TENTO LISTENER NEZABRAŇUJE ZNIČENÍ ARMORSTANDU POMOCÍ PŘÍKAZŮ ČI JINÝCH PLUGINŮ!
    */


    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        Entity ent = e.getEntity();

        // Check if the entity contains a scoreboard tag
        if (ent.getType().equals(EntityType.ARMOR_STAND)){
            String ID = null;
            for (String s : ent.getScoreboardTags()) {
                if (s.startsWith("Start")) {
                    ID = s.substring(5);
                    break;
                }else if(s.startsWith("End")){
                    ID = s.substring(3);
                    break;
                }
            }
            if(ID  != null){
                if(TimeTrialPlugin.getPlugin().getTtConfig().isConfigurationSection(ID)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
