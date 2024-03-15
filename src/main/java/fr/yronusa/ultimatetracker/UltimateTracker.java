package fr.yronusa.ultimatetracker;

import fr.yronusa.ultimatetracker.Commands.Command;
import fr.yronusa.ultimatetracker.Config.Config;
import fr.yronusa.ultimatetracker.Database.Database;
import fr.yronusa.ultimatetracker.Database.Initializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class UltimateTracker extends JavaPlugin {

    private static UltimateTracker instance;

    public static Player yro;




    public static UltimateTracker getInstance(){
        return UltimateTracker.instance;
    }

    public static boolean database = true;

    // Defines the maximum length of last inventories list.
    public int inventoryListLength;
    @Override
    public void onEnable() {
        UltimateTracker.instance = this;
        if(!Bukkit.getOnlinePlayers().isEmpty()){
            yro = Bukkit.getPlayer("Yronusa2000");
        }
        saveDefaultConfig();
        Config.load();
        registerEvents();
        registerCommands();
        database = Initializer.initializeDatabase();


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


        System.out.println("[UltimateTracker] Closing database connection.");
        try {
            if(Database.connection != null && !Database.connection.isClosed()) Database.connection.close();
            if(Initializer.sqlServerConnection != null && !Initializer.sqlServerConnection.isClosed()) Initializer.sqlServerConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getVersion(){
        return "0.1";
    }
}
