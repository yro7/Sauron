package fr.yronusa.ultimatetracker;

import fr.yronusa.ultimatetracker.Commands.Command;
import fr.yronusa.ultimatetracker.Config.TrackingRule;
import mc.obliviate.inventory.InventoryAPI;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import javax.sound.midi.Track;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executor;

public final class UltimateTracker extends JavaPlugin {

    private static UltimateTracker instance;

    public static List<TrackingRule> rules;

    public static List<TrackingRule> getTrackingRules(){
        return UltimateTracker.rules;
    }
    public static UltimateTracker getInstance(){
        return UltimateTracker.instance;
    }

    static boolean database = true;

    // Defines the maximum length of last inventories list.
    public int inventoryListLength;
    @Override
    public void onEnable() {

        System.out.println("prout");
        // Plugin startup logic
        UltimateTracker.instance = this;
        saveDefaultConfig();

        registerEvents();
        registerCommands();


        UltimateTracker.rules = TrackingRule.getTrackingRulesFromConfig();

        verifyDatabase();


    }

    public static Timestamp getActualDate(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime roundedDateTime = currentDateTime.truncatedTo(ChronoUnit.MINUTES);
        System.out.println("actual date:");
        System.out.println(Timestamp.valueOf(roundedDateTime).toString());
        return Timestamp.valueOf(roundedDateTime);
    }

    private void verifyDatabase() {

       /** try{
            Database.createDatabase();
        }
        catch(Exception e){
            System.out.print("[ULTIMATE TRACKER] Error trying to connect to database:");
            e.printStackTrace();

        }

        try {
            Database.createTables();
        }
        catch(Exception e){
            System.out.print("[ULTIMATE TRACKER] Error trying to create the tables:");
            e.printStackTrace();
        }

**/
    }

    private void registerCommands() {

        this.getCommand("ut").setExecutor(new Command());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new Listener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Executor getMainThreadExecutor(Plugin plugin, BukkitScheduler scheduler){
        Validate.notNull(plugin, "Plugin cannot be null!");
        return (command) -> {
            Validate.notNull(command, "Command cannot be null!");
            scheduler.runTask(plugin, command);
        };
    }
}
