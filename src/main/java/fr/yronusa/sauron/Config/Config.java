package fr.yronusa.sauron.Config;

import fr.yronusa.sauron.Database.Database;
import fr.yronusa.sauron.Sauron;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {

    public static FileConfiguration config;

    public static List<TrackingRule> trackingRules;

    // Plugin settings
    public static int updateInterval;
    public static boolean saveItemData;

    public static int automaticUpdateInterval;

    /**
     * Should the plugin apply a tracker on {@link org.bukkit.inventory.ItemStack} corresponding to a {@link TrackingRule} with amount > 1 ?
     * <p>
     * CAUTION: Improper use may cause issues with the plugin.
     */
    public static boolean trackStackedItems;

    /**
     Should the plugin clear stacked tracked items ? A item is considered as "tracked" if it has a sauron_id nbt.
      */
    public static boolean clearStackedItems;

    public static boolean automaticInventoryUpdating;
    public static int delayBetweenChecks;
    public static int delayBetweenPlayers;
    public static long delayBetweenItems;


    // Database credentials

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
    public static String blacklistedItemPlayer;


    public static boolean load(){
        try{
            config = Sauron.getInstance().getConfig();;
            loadMessages();
            loadDatabase();
            loadSettings();
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
        updateInterval = config.getInt("settings.update-interval");
        clearStackedItems = config.getBoolean("settings.clear-stacked-items");
        trackStackedItems = config.getBoolean("settings.track-stacked-items");
        saveItemData = config.getBoolean("settings.save-item-data");

        automaticInventoryUpdating = config.getBoolean("settings.automatic-inventory-updating.enabled");
        delayBetweenPlayers = config.getInt("settings.automatic-inventory-updating.delay-between-players");
        delayBetweenItems = config.getLong("settings.automatic-inventory-updating.delay-between-items");
        delayBetweenChecks = config.getInt("settings.automatic-inventory-updating.delay-between-checks");

    }

    private static void loadMessages() {
        notTracked = get("messages.not-tracked");
        dupeFoundPlayer = get("messages.dupe-found-player");
        stackedItemPlayer = get("messages.stacked-item-player");
        blacklistedItemPlayer = get("messages.blacklisted-item-found-player");
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
