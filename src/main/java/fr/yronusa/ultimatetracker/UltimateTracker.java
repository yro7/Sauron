package fr.yronusa.ultimatetracker;

import fr.yronusa.ultimatetracker.Commands.Command;
import fr.yronusa.ultimatetracker.Config.Config;
import fr.yronusa.ultimatetracker.Database.Database;
import fr.yronusa.ultimatetracker.Database.Initializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class UltimateTracker extends JavaPlugin {

    private static UltimateTracker instance;



    public static UltimateTracker getInstance(){
        return UltimateTracker.instance;
    }

    public static boolean database = true;

    // Defines the maximum length of last inventories list.
    public int inventoryListLength;
    @Override
    public void onEnable() {
        UltimateTracker.instance = this;
        Config.load();
        saveDefaultConfig();
        registerEvents();
        registerCommands();
        database = Initializer.verifyDatabase();


    }

    public static Timestamp getActualDate(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime roundedDateTime = currentDateTime.truncatedTo(ChronoUnit.SECONDS);
        return Timestamp.valueOf(roundedDateTime);
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


        try {
            if(Database.connection != null) Database.getConnection().close();
            if(Initializer.connectionWithoutDtb != null) Initializer.connectionWithoutDtb.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getVersion(){
        return "0.1";
    }
}
