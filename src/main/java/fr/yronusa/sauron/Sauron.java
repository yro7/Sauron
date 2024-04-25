/**
 *
 *
 * This project is licensed under the GNU General Public License version 3 (GNU GPL-3.0).
 * See the [LICENSE](LICENSE) file for details.
 *
 *
 *
**/
package fr.yronusa.sauron;

import fr.yronusa.sauron.Commands.Command;
import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Database.Database;
import fr.yronusa.sauron.Database.Initializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author yronusa
 */

public final class Sauron extends JavaPlugin {

    private static Sauron instance;

    public static Sauron getInstance(){
        return Sauron.instance;
    }

    public static boolean database;


    @Override
    public void onEnable() {
        Sauron.instance = this;
        saveDefaultConfig();
        Config.load();
        registerEvents();
        registerCommands();

        database = Initializer.initializeDatabase();
        Log.initialize();
        Cache.initialize();
        CrashHandler.initialize();
        Tracker.initialize();



    }

    public static Timestamp getActualDate(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime roundedDateTime = currentDateTime.truncatedTo(ChronoUnit.SECONDS);
        return Timestamp.valueOf(roundedDateTime);
    }

    private void registerCommands() {
        this.getCommand("sauron").setExecutor(new Command());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new Tracker(), this);
        getServer().getPluginManager().registerEvents(new Listener(), this);
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic

        CrashHandler.close();

        Log.console("Closing database connection.", Log.Level.HIGH);
        try {
            if(Database.connection != null && !Database.connection.isClosed()) Database.connection.close();
            if(Initializer.sqlServerConnection != null && !Initializer.sqlServerConnection.isClosed()) Initializer.sqlServerConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getVersion(){
        return "1.2";
    }
}
