package fr.yronusa.sauron;

import fr.yronusa.sauron.Commands.Command;
import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Database.Database;
import fr.yronusa.sauron.Database.Initializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class Sauron extends JavaPlugin {

    private static Sauron instance;

    public static Player yro;




    public static Sauron getInstance(){
        return Sauron.instance;
    }

    public static boolean database = true;

    // Defines the maximum length of last inventories list.
    public int inventoryListLength;
    @Override
    public void onEnable() {
        Sauron.instance = this;
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


        System.out.println("[Sauron] Closing database connection.");
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
