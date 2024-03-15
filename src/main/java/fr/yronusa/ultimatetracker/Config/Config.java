package fr.yronusa.ultimatetracker.Config;

import fr.yronusa.ultimatetracker.Database.Database;
import fr.yronusa.ultimatetracker.UltimateTracker;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {

    public static FileConfiguration config;

    public static List<TrackingRule> trackingRules;

    // PLugin settings
    public static int delay;

    public static boolean trackStackedItems;
    public static boolean clearStackedItems;

    // Database data

    public static Database.TYPE databaseType;
    public static String databaseHost;
    public static int databasePort;

    public static String databaseName;
    public static String databaseUser;
    public static String databasePassword;

    // Messages

    public static String notTracked;
    public static String dupeFoundPlayer;
    public static String stackedItemPlayer;
    public static List<String> helpCommand;
    public static String reloadSuccessful;

    public static String trackAlreadyTracked;
    public static String trackStacked;
    public static String trackSuccess;
    public static String trackFailed;
    public static String trackEmpty;

    public static String insufficientPermission;


    public static boolean load(){
        try{
            config = UltimateTracker.getInstance().getConfig();;
            loadMessages();
            loadDatabase();
            loadSettings();
            System.out.println("getting tracking rules...");
            trackingRules = TrackingRule.getTrackingRulesFromConfig();

            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static void loadDatabase() {
        databaseType = Database.TYPE.valueOf(get("database.type").toLowerCase());
        databaseHost = get("database.host");
        databasePort = config.getInt("database.port");
        databaseName = get("database.database");
        databaseUser = get("database.user");
        databasePassword = get("database.password");
    }

    private static void loadSettings(){
        delay = config.getInt("settings.delay");
        clearStackedItems = config.getBoolean("settings.clear-stacked-items");
        trackStackedItems = config.getBoolean("settings.track-stacked-items");
    }

    private static void loadMessages() {
        notTracked = get("messages.not-tracked");
        dupeFoundPlayer = get("messages.dupe-found-player");
        stackedItemPlayer = get("messages.stacked-item-player");
        helpCommand = config.getStringList("messages.commands.help");
        reloadSuccessful = get("messages.commands.reload");
        insufficientPermission = get("messages.commands.insufficient-permission");

        trackAlreadyTracked = get("messages.commands.track.already-tracked");
        trackStacked = get("messages.commands.track.stacked");
        trackSuccess = get("messages.commands.track.success");
        trackFailed = get("messages.commands.track.fail");
        trackEmpty = get("messages.commands.track.empty");

    }

    public static String get(String s){
        return config.getString(s);
    }


}
