package fr.yronusa.ultimatetracker;

import fr.yronusa.ultimatetracker.Commands.Command;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class UltimateTracker extends JavaPlugin {

    private static UltimateTracker instance;
    public static UltimateTracker getInstance(){
        return UltimateTracker.instance;
    }

    // Defines the maximum length of last inventories list.
    public int inventoryListLength;
    @Override
    public void onEnable() {
        // Plugin startup logic
        UltimateTracker.instance = this;
        new InventoryAPI(this).init();
        saveDefaultConfig();
        registerEvents();
        registerCommands();
        verifyDatabase();


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
