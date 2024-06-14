package fr.yronusa.sauron;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Database.Database;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Timestamp;

public class CrashHandler {

    public static Timestamp previousOldestItemUpdated;
    public static Timestamp oldestItemUpdated;
    public static boolean hasCrashed;

    //public static HashSet<TrackedItem>

    public static void initialize(){
        
        FileConfiguration config = Config.getConfig();

        hasCrashed = config.getBoolean("crash-handler.crashed");
        String previousOldestItemString = config.getString("crash-handler.oldest-item");
        try{
            System.out.println(previousOldestItemString);
            previousOldestItemUpdated = Timestamp.valueOf(previousOldestItemString);
        } catch(IllegalArgumentException e){
            previousOldestItemUpdated = Sauron.getActualDate();
            e.printStackTrace();
        }

        if(hasCrashed){
            Timestamp actualDate = Sauron.getActualDate();
            Log.console("§cA crash has been detected. Added new crash date whitelist :" +
                    " \n " + previousOldestItemUpdated + " to " + actualDate,
                    Log.Level.HIGH);
            Database.addCrashDate(previousOldestItemUpdated, actualDate);
        }

        oldestItemUpdated = Sauron.getActualDate();
        System.out.println("Setting crash handler crashed to true.");
        config.set("crash-handler.crashed", true);
        Config.getConfig().set("crash-handler.oldest-item", oldestItemUpdated.toString());
        Sauron.getInstance().saveConfig();


    }

    public static void check(TrackedItem item){
        Timestamp itemLastUpdate = item.getLastUpdateItem();
        System.out.print("CHECK ITEM : " + item.getLastUpdateItem());
        System.out.println("actual : " + oldestItemUpdated);
        if(itemLastUpdate.before(CrashHandler.oldestItemUpdated)){
            System.out.print("updating item");
            CrashHandler.oldestItemUpdated = itemLastUpdate;
            Config.getConfig().set("crash-handler.oldest-item", oldestItemUpdated.toString());
            Sauron.getInstance().saveConfig();
        }

    }

    public static void close(){
        try{
            FileConfiguration config = Config.getConfig();
            config.set("crash-handler.crashed", false);
            config.set("crash-handler.oldest-item", oldestItemUpdated.toString());
            Sauron.getInstance().saveConfig();
            Log.console("Successfully closed crash handler.", Log.Level.HIGH);

        } catch(Exception e){
            e.printStackTrace();
        }


    }




}
