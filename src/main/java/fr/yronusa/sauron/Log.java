package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The logger of the plugin.
 */
public class Log {

    private static Logger logger;

    public static Formatter formatter;

    public static enum Level {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        DEBUG,
    }

    public static void initialize(){
        String logsFolderPath = Sauron.getInstance().getDataFolder().getAbsolutePath() + "/logs";
        File logDir = new File(logsFolderPath);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        Logger newLogger = Logger.getLogger("Sauron");
        formatter = createXMLFormatter();

        try {
            FileHandler fileHandler = new FileHandler(logsFolderPath+"/logs.log");
            fileHandler.setFormatter(formatter);
            newLogger.addHandler(fileHandler);
        } catch (IOException e) {
            newLogger.severe("[Sauron] Failed to create log file handler: " + e.getMessage());
        }

        logger = newLogger;
    }

    public static void sendMessageAdmin(String msg){
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
        for(Player p : players){
            if(p.hasPermission("sauron.alert")){
                p.sendMessage(msg);
            }
        }

    }

    public static void test(){
        logger.info("This is an test info message..");
        logger.warning("This is a test warning message.");
        logger.severe("This is a test severe message.");
    }

    public static void dupe(UUID itemID, Player player, Location location, String base64, Timestamp item, Timestamp database){
        String name = "N/A";
        if(player != null){
            name = player.getName();
        }
        log(itemID, player, location, base64, item, database, "Duplicated item found");
        sendMessageAdmin("§c[Sauron] Duplicated item found at " + toString(location) + " by player " + name);
    }

    public static void stackedFound(UUID itemId, Player player, Location location, int quantity, String base64)    {
        logger.log(java.util.logging.Level.SEVERE, "Stacked items found. Quantity : " +
                quantity,new Object[]{itemId,player, toString(location), base64});
        sendMessageAdmin("§c[Sauron] Stacked item found at " + toString(location) + " by player " + player.getName() + ". quantity : " + quantity);
    }

    public static void blacklistFound(UUID itemId, Player player, Location location, String base64)    {
        logger.log(java.util.logging.Level.SEVERE, "Blacklisted item found.",new Object[]{itemId,player, toString(location), base64});
        sendMessageAdmin("§c[Sauron] Blacklisted item found at " + toString(location) + " by player " + player.getName());
    }

    public static void illegalItemFound(Player player, Location location, String base64) {
        logger.log(java.util.logging.Level.SEVERE, "Illegal item found",new Object[]{"null",player, toString(location), base64});
        sendMessageAdmin("§c[Sauron] Illegal item found at " + toString(location) + " by player " + player.getName());

    }


    public static void log(UUID itemID, Player player, Location location, String base64, Timestamp item, Timestamp database, String message){

        String name = "N/A";
        if(player != null){
            name = player.getName();
        }
        logger.log(java.util.logging.Level.SEVERE, "[SAURON] " + message, new Object[]{itemID, name,
                toString(location), base64, item.toString(), database.toString()});
    }

    /**
     *   LOW = HIGH /
     *   MEDIUM = HIGH, MEDIUM /
     *   HIGH = HIGH, MEDIUM, LOW
     * @param message
     * @param level
     */
    public static void console(String message, Level level){
        Level configLvl = Config.verboseLevel;
        if (configLvl == Level.NONE ||
            configLvl == Level.LOW && level == Level.LOW ||
            configLvl == Level.MEDIUM && level == Level.LOW ||
            configLvl != Level.DEBUG && level == Level.DEBUG) {
            return;
        }

        System.out.println("[SAURON] " + message);


    }





    private static Formatter createXMLFormatter() {
        return new Formatter() {
            public String format(LogRecord record) {
                StringBuilder builder = new StringBuilder();
                builder.append("Date: ").append(new Date(record.getMillis())).append("\n");
                builder.append("  Severity: ").append(record.getLevel()).append("\n");
                builder.append("  Object UUID: ").append(getParam(record, 0)).append("\n");
                builder.append("  Player: ").append(getParam(record, 1)).append("\n");
                builder.append("  Location: ").append(getParam(record,2)).append("\n");
                builder.append("  Item Base 64 : ").append(getParam(record, 3)).append("\n");
                builder.append("  Item's timestamp : ").append(getParam(record, 4)).append("\n");
                builder.append("  Database timestamp : ").append(getParam(record, 5)).append("\n");

                builder.append("  Info: ").append(formatMessage(record)).append("\n");
                builder.append("\n");
                return builder.toString();
            }
        };
    }

    private static String getParam(LogRecord record, int param) {
        Object[] parameters = record.getParameters();
        if (parameters != null && 0 <= param && param < parameters.length) {
            return parameters[param].toString();
        }
        return "";
    }

    public static String toString(Location location){
      return location.getWorld().getName() + " at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

}

