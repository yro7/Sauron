package fr.yronusa.ultimatetracker.Config;

import fr.yronusa.ultimatetracker.UltimateTracker;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {

    public static FileConfiguration config;

    public static List<TrackingRule> trackingRules;

    // PLugin settings
    public static int delay;

    // Database data
    public static String databaseHost;
    public static int databasePort;

    public static String databaseName;
    public static String databaseUser;
    public static String databasePassword;

    // Messages
    public static String dupeFoundPlayer;
    public static List<String> helpCommand;
    public static String reloadSuccessful;

    public static String trackAlreadyTracked;
    public static String trackStacked;
    public static String trackSuccess;
    public static String trackFailed;
    public static String trackEmpty;





public static boolean load(){
        try{
            config = UltimateTracker.getInstance().getConfig();;



            databaseHost = config.getString("database.host");
            databasePort = config.getInt("database.port");
            databaseName = config.getString("database.database");
            databaseUser = config.getString("database.user");
            databasePassword = config.getString("database.password");



            dupeFoundPlayer = config.getString("messages.dupe-found-player");
            helpCommand = config.getStringList("messages.commands.help");
            reloadSuccessful = config.getString("messages.commands.reload");

            trackAlreadyTracked = config.getString("messages.commands.track.already-tracked");
            trackStacked = config.getString("messages.commands.track.stacked");
            trackSuccess = config.getString("messages.commands.track.success");
            trackFailed = config.getString("messages.commands.track.fail");
            trackEmpty = config.getString("messages.commands.track.empty");



            delay = config.getInt("settings.delay");

            trackingRules = TrackingRule.getTrackingRulesFromConfig();



            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
