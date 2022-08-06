package me.technyk.timetrialplugin;

import me.technyk.timetrialplugin.commands.timetrialCommand;
import me.technyk.timetrialplugin.listeners.blockDestroy;
import me.technyk.timetrialplugin.listeners.entityDamage;
import me.technyk.timetrialplugin.listeners.timeTrialOpen;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;

public final class TimeTrialPlugin extends JavaPlugin {

    private static TimeTrialPlugin plugin;
    private final File ttConfigFile = new File(getDataFolder(), "ttStorage.yml");
    private final FileConfiguration ttConfig = YamlConfiguration.loadConfiguration(ttConfigFile);


    @Override
    public void onEnable() {
        // Plugin startup logic

        if(!ttConfigFile.exists()) {
            saveResource("ttStorage.yml", false);
        }

        refreshMarkers();

        getCommand("timetrial").setExecutor(new timetrialCommand());
        getServer().getPluginManager().registerEvents(new timeTrialOpen(), this);
        getServer().getPluginManager().registerEvents(new blockDestroy(), this);
        getServer().getPluginManager().registerEvents(new entityDamage(), this);

        new timeTrialOpen().startTimer(this);

        plugin = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public File getTtConfigFile(){
        return ttConfigFile;
    }

    public FileConfiguration getTtConfig() {
        return ttConfig;
    }

    public Integer getLastId(){
        if(ttConfigFile.exists()){
            return ttConfig.getInt("lastId");
        }
        return -1;
    }

    public Location str2loc(String str){
        String[] splitStr = str.split(":");
        Location loc = new Location(getServer().getWorld(splitStr[0]),0,0,0);
        loc.setX(Double.parseDouble(splitStr[1]));
        loc.setY(Double.parseDouble(splitStr[2]));
        loc.setZ(Double.parseDouble(splitStr[3]));
        return loc;
    }

    public String loc2str(Location loc){
        return loc.getWorld().getName()+":"+loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
    }

    public ArrayList<String> refreshMarkers(){

        ArrayList<String> problems = new ArrayList<>();
        problems.add("0");

        for(int i = 0; i < this.getLastId() + 1; i++){
            if(this.getTtConfig().isConfigurationSection(String.valueOf(i))){
                String LocToParse = this.getTtConfig().getString(i + ".StartLocation");
                Location LocToCheck;
                LocToCheck = this.str2loc(LocToParse);
                Block BlockToCheck = LocToCheck.getBlock();

                if(!(BlockToCheck.getType().equals(Material.BLACK_BANNER))){
                    if(!BlockToCheck.isEmpty()){
                        problems.add(LocToParse);
                    }else{
                        // create a block at the location
                        BlockToCheck.setType(Material.BLACK_BANNER);
                        Banner banner = (Banner) BlockToCheck.getState();
                        banner.addPattern(new Pattern(DyeColor.WHITE, PatternType.GLOBE));
                        banner.update();

                        // add a persistent data container
                        BlockState blockState = BlockToCheck.getState();

                        TileState tileState = (TileState) blockState;

                        PersistentDataContainer container = tileState.getPersistentDataContainer();

                        container.set(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttid"), PersistentDataType.INTEGER, i);
                        container.set(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttdir"), PersistentDataType.STRING, "start");
                        tileState.update();

                        problems.set(0, (String.valueOf(Integer.parseInt(problems.get(0)) + 1)));
                    }
                }
                boolean found = false;
                for(World w : Bukkit.getWorlds()){
                    for(Entity e : w.getEntities()){
                        if (e.getType().equals(EntityType.ARMOR_STAND)){
                            for (String s : e.getScoreboardTags()) {
                                if ((s.startsWith("Start")) && (s.substring(5).equals(String.valueOf(i))) ) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                    }
                }
                if(!found){
                    problems.set(0, (String.valueOf(Integer.parseInt(problems.get(0)) + 1)));
                    ArmorStand StartHologram = (ArmorStand) BlockToCheck.getWorld().spawnEntity(BlockToCheck.getLocation().add(0.5,2,0.5), EntityType.ARMOR_STAND);

                    StartHologram.setVisible(false);
                    StartHologram.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + this.getTtConfig().getString(i + ".Name"));
                    StartHologram.setCustomNameVisible(true);
                    StartHologram.setGravity(false);
                    StartHologram.setCollidable(false);
                    StartHologram.setInvulnerable(true);
                    StartHologram.setMarker(true);
                    StartHologram.addScoreboardTag("Start" + i);

                    ArmorStand StartHologram2 = (ArmorStand) BlockToCheck.getWorld().spawnEntity(BlockToCheck.getLocation().add(0.5,1.75,0.5), EntityType.ARMOR_STAND);

                    StartHologram2.setVisible(false);
                    StartHologram2.setCustomName(ChatColor.GOLD + "(Start)");
                    StartHologram2.setCustomNameVisible(true);
                    StartHologram2.setGravity(false);
                    StartHologram2.setCollidable(false);
                    StartHologram2.setInvulnerable(true);
                    StartHologram2.setMarker(true);
                    StartHologram2.addScoreboardTag("Start" + i);
                }

                LocToParse = this.getTtConfig().getString(i + ".EndLocation");
                if(!(LocToParse.equals("None"))) {
                    LocToCheck = this.str2loc(LocToParse);
                    BlockToCheck = LocToCheck.getBlock();

                    if (!(BlockToCheck.getType().equals(Material.BLUE_BANNER))) {
                        if (!BlockToCheck.isEmpty()) {
                            problems.add(LocToParse);
                        } else {
                            // create a block at the location
                            BlockToCheck.setType(Material.BLUE_BANNER);
                            Banner banner = (Banner) BlockToCheck.getState();
                            banner.addPattern(new Pattern(DyeColor.WHITE, PatternType.GLOBE));
                            banner.update();

                            // add a persistent data container
                            BlockState blockState = BlockToCheck.getState();

                            TileState tileState = (TileState) blockState;

                            PersistentDataContainer container = tileState.getPersistentDataContainer();

                            container.set(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttid"), PersistentDataType.INTEGER, i);
                            container.set(new NamespacedKey(TimeTrialPlugin.getPlugin(), "ttdir"), PersistentDataType.STRING, "end");
                            tileState.update();

                            problems.set(0, (String.valueOf(Integer.parseInt(problems.get(0)) + 1)));
                        }
                    }
                    found = false;
                    for (World w : Bukkit.getWorlds()) {
                        for (Entity e : w.getEntities()) {
                            if (e.getType().equals(EntityType.ARMOR_STAND)) {
                                for (String s : e.getScoreboardTags()) {
                                    if ((s.startsWith("End")) && (s.substring(3).equals(String.valueOf(i)))) {
                                        found = true;
                                        break;
                                    }
                                }
                            }

                        }
                    }
                    if (!found) {
                        problems.set(0, (String.valueOf(Integer.parseInt(problems.get(0)) + 1)));

                        ArmorStand StartHologram = (ArmorStand) BlockToCheck.getWorld().spawnEntity(BlockToCheck.getLocation().add(0.5, 2, 0.5), EntityType.ARMOR_STAND);

                        StartHologram.setVisible(false);
                        StartHologram.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + this.getTtConfig().getString(i + ".Name"));
                        StartHologram.setCustomNameVisible(true);
                        StartHologram.setGravity(false);
                        StartHologram.setCollidable(false);
                        StartHologram.setInvulnerable(true);
                        StartHologram.setMarker(true);
                        StartHologram.addScoreboardTag("End" + i);

                        ArmorStand StartHologram2 = (ArmorStand) BlockToCheck.getWorld().spawnEntity(BlockToCheck.getLocation().add(0.5, 1.75, 0.5), EntityType.ARMOR_STAND);

                        StartHologram2.setVisible(false);
                        StartHologram2.setCustomName(ChatColor.GOLD + "(Konec)");
                        StartHologram2.setCustomNameVisible(true);
                        StartHologram2.setGravity(false);
                        StartHologram2.setCollidable(false);
                        StartHologram2.setInvulnerable(true);
                        StartHologram2.setMarker(true);
                        StartHologram2.addScoreboardTag("End" + i);
                    }
                }

            }
        }

        return problems;
    }

    public static TimeTrialPlugin getPlugin() {
        return plugin;
    }
}
