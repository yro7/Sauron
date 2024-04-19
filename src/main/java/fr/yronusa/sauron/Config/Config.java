package fr.yronusa.sauron.Config;

import fr.yronusa.sauron.Database.Database;
import fr.yronusa.sauron.Log;
import fr.yronusa.sauron.Sauron;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Config {

    public static FileConfiguration config;

    public static List<TrackingRule> trackingRules;

    public static List<TrackingRule> illegalItemRules;

    // Plugin settings

    public static boolean enableIllegalItemsLookup;
    public static boolean enableItemsTracking;
    public static long inHandUpdateInterval;
    public static boolean saveItemData;
    public static Log.Level verboseLevel;

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
    public static long delayBetweenChecks;
    public static long delayBetweenPlayers;
    public static long delayBetweenItems;

    public static long containerUpdateInterval;


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
    public static String illegalItemPlayer;
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
            trackingRules = TrackingRule.getTrackingRulesFromConfig("items-to-track");
            illegalItemRules = TrackingRule.getTrackingRulesFromConfig("illegal-items");
            enableIllegalItemsLookup = !illegalItemRules.isEmpty();
            enableItemsTracking = !trackingRules.isEmpty();

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
        inHandUpdateInterval = config.getInt("settings.in-hand-update-interval");
        clearStackedItems = config.getBoolean("settings.clear-stacked-items");
        trackStackedItems = config.getBoolean("settings.track-stacked-items");
        saveItemData = config.getBoolean("settings.save-item-data");
        verboseLevel = Log.Level.valueOf(config.getString("settings.verbose-level"));


        automaticInventoryUpdating = config.getBoolean("settings.automatic-inventory-updating.enabled");
        delayBetweenPlayers = config.getInt("settings.automatic-inventory-updating.delay-between-players");
        delayBetweenItems = config.getLong("settings.automatic-inventory-updating.delay-between-items");
        delayBetweenChecks = config.getInt("settings.automatic-inventory-updating.delay-between-checks");
        containerUpdateInterval = config.getLong("settings.container-update-interval");
    }

    private static void loadMessages() {
        notTracked = get("messages.not-tracked");
        dupeFoundPlayer = get("messages.dupe-found-player");
        stackedItemPlayer = get("messages.stacked-item-found-player");
        blacklistedItemPlayer = get("messages.blacklisted-item-found-player");
        illegalItemPlayer = get("messages.illegal-item-found-player");
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

    public static boolean itemStackIsToTrack(ItemStack item) {
        return itemStackMatchesRules(item, Config.trackingRules);
    }
    public static boolean itemStackIsIllegal(ItemStack item){
        return itemStackMatchesRules(item, Config.illegalItemRules);
    }

    public static boolean itemStackMatchesRules(ItemStack item, List<TrackingRule> list){
        if(item == null) return false;

        for(TrackingRule trackingRule : list){
            if(trackingRule.test(item)) return true;
        }

        return false;
    }


}
