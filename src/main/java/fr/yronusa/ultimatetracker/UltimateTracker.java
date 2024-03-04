package fr.yronusa.ultimatetracker;

import fr.yronusa.ultimatetracker.Commands.Command;
import fr.yronusa.ultimatetracker.Config.TrackingRule;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sound.midi.Track;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class UltimateTracker extends JavaPlugin {

    private static UltimateTracker instance;

    public static List<TrackingRule> rules;

    public static List<TrackingRule> getTrackingRules(){
        return UltimateTracker.rules;
    }
    public static UltimateTracker getInstance(){
        return UltimateTracker.instance;
    }

    static boolean database = false;

    // Defines the maximum length of last inventories list.
    public int inventoryListLength;
    @Override
    public void onEnable() {
        // Plugin startup logic
        UltimateTracker.instance = this;
        saveDefaultConfig();

        registerEvents();
        registerCommands();


        UltimateTracker.rules = TrackingRule.getTrackingRulesFromConfig();

        verifyDatabase();


    }

    public static String getActualDate(){
        String format = "yyyy.MM.dd.HH.mm";
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return currentDateTime.format(formatter);
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
}
