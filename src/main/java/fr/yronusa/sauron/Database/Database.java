package fr.yronusa.sauron.Database;

import fr.yronusa.sauron.Event.DatabaseItemAddedEvent;
import fr.yronusa.sauron.Event.DatabaseItemBlacklistEvent;
import fr.yronusa.sauron.InventoryLocation;
import fr.yronusa.sauron.Log;
import fr.yronusa.sauron.Sauron;
import fr.yronusa.sauron.TrackedItem;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.List;
import java.util.UUID;

import static fr.yronusa.sauron.Config.Config.*;


public class Database {

    public static Connection connection;

    public enum TYPE {
        mysql,
        mariadb,
        postgresql,
        oracle,
        sqlserver,
        sqlite
    }


    public static Connection getConnection() throws SQLException {
        if(!connection.isValid(2)){
            Log.sendMessageAdmin("§c[Sauron] New connection to sauron's database.");
            connect();
        }
        return connection;
    }

    public static void connect() throws SQLException {
        Database.connection = DriverManager.getConnection(Database.getJDBC(), databaseUser, databasePassword);
    }


    public static String getJDBC(){
         return switch (databaseType) {
            case oracle -> "jdbc:oracle:thin:@" + databaseHost + ":" + databasePort + ":orcl";
            case sqlserver ->
                    "jdbc:sqlserver://" + databaseHost + ":" + databasePort + ";databaseName=" + databaseName;
            case sqlite -> {
                String path = Sauron.getInstance().getDataFolder().getAbsolutePath();
                yield "jdbc:sqlite:" + path + databaseName + ".db";
            }
            default -> "jdbc:" + databaseType + "://"
                    + databaseHost + ":" + databasePort + "/" + databaseName + "?useSSL=false";
        };
    }


    public static void add(TrackedItem trackedItem) {
        String itemBase64 = trackedItem.getBase64();

        String statement = "INSERT INTO TRACKED_ITEMS (UUID, ITEMBASE64, LAST_UPDATE, LAST_INVENTORIES, IS_BLACKLISTED) VALUES (?, ?, ?, ?, ?)";
        Bukkit.getScheduler().runTaskAsynchronously(Sauron.getInstance(), new Runnable() {
            @Override
            public void run() {

                try {
                    Timestamp time = trackedItem.getLastUpdateItem();
                    Connection conn = getConnection();
                    PreparedStatement preparedStatement = conn.prepareStatement(statement);
                    preparedStatement.setString(1, trackedItem.getOriginalID().toString());
                    preparedStatement.setString(2, "");
                    if(saveItemData) preparedStatement.setString(2, itemBase64);
                    preparedStatement.setTimestamp(3, time);
                    preparedStatement.setString(4, "TODO");
                    preparedStatement.setInt(5, 0);

                    int i = preparedStatement.executeUpdate();
                    if (i > 0) {
                        DatabaseItemAddedEvent itemAddedEvent = new DatabaseItemAddedEvent(trackedItem, time);
                        // Necessary because in the newest version of Spigot, Events can't be called from async thread.
                        Bukkit.getScheduler().runTask(Sauron.getInstance(), () -> Bukkit.getPluginManager().callEvent(itemAddedEvent));
                    } else {
                        System.out.println("[SAURON] An error has occured while inserting a new item in database.");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }


            }
        });

    }

    public static Timestamp getLastUpdate(UUID uuid){
        String statement = "SELECT LAST_UPDATE FROM TRACKED_ITEMS WHERE UUID = ?";
        Timestamp lastUpdateTimestamp = null;
        try {
            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            // Check if the result set has data
            if (resultSet.next()) {
                // Retrieve the last update timestamp from the result set
                lastUpdateTimestamp = resultSet.getTimestamp("LAST_UPDATE");
                // Print or use the timestamp as needed
            } else {
                System.out.println("[Sauron] No data found for UUID: " + uuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastUpdateTimestamp;
    }
    public static List<InventoryLocation> getLastInventories(UUID uuid){
        String sqlSelectTrackedItem= "SELECT * FROM SAVED_ITEMS WHERE UUID = " + uuid.toString();

        final String[] res = new String[0];
        Bukkit.getScheduler().runTaskAsynchronously(Sauron.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = getConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlSelectTrackedItem);
                    ResultSet rs = ps.executeQuery(); {
                        while (rs.next()) {
                            res[0] = rs.getString("LAST_UPDATE");
                        }


                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }


        });
        return null;
    }
    public static void update(TrackedItem item, Timestamp newDate) {

        String verif = "SELECT 1 FROM TRACKED_ITEMS WHERE UUID = ?";
        String statement = "UPDATE TRACKED_ITEMS SET LAST_UPDATE = ? WHERE UUID = ?";
        Bukkit.getScheduler().runTaskAsynchronously(Sauron.getInstance(), new Runnable() {
            @Override
            public void run() {

                try {
                    Connection conn = getConnection();
                    String uuid = item.getOriginalID().toString();
                    PreparedStatement verifPresence = conn.prepareStatement(verif);
                    verifPresence.setString(1, uuid);

                    PreparedStatement preparedStatement = conn.prepareStatement(statement);
                    preparedStatement.setTimestamp(1, newDate);
                    preparedStatement.setString(2, uuid);

                    ResultSet resultSet = verifPresence.executeQuery();
                    if(!resultSet.next()){
                        Database.add(item);
                        return;
                    }


                    int i = preparedStatement.executeUpdate();
                    if (i > 0) {
                        System.out.println("ROW UPDATED");
                    } else {
                        System.out.println("ROW NOT UPDATED");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }


            }
        });


    }

    public static boolean isDuplicated(TrackedItem item){
        Timestamp databaseTimestamp = Database.getLastUpdate(item.getOriginalID());
        Timestamp itemTimestamp = item.getLastUpdateItem();
        return itemTimestamp.before(databaseTimestamp);
    }

    public static void blacklist(TrackedItem trackedItem, UUID oldID) {

        String statement = "UPDATE TRACKED_ITEMS SET IS_BLACKLISTED = ? WHERE UUID = ?";
        Bukkit.getScheduler().runTaskAsynchronously(Sauron.getInstance(), new Runnable() {
            @Override
            public void run() {

                try {
                    Connection conn = getConnection();
                    PreparedStatement preparedStatement = conn.prepareStatement(statement);
                    preparedStatement.setInt(1, 1);
                    preparedStatement.setString(2, oldID.toString());
                    System.out.println("blacklisting item blabla");
                    int i = preparedStatement.executeUpdate();
                    if (i > 0) {
                        DatabaseItemBlacklistEvent blacklistEvent = new DatabaseItemBlacklistEvent(trackedItem);
                        // Necessary because in the newest version of Spigot, Event can't be called from async thread.
                        Bukkit.getScheduler().runTask(Sauron.getInstance(), () -> Bukkit.getPluginManager().callEvent(blacklistEvent));
                    } else {
                        System.out.println("[Sauron] ITEM NOT BLACKLISTED");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }


            }
        });

        DatabaseItemBlacklistEvent databaseItemBlacklistEvent = new DatabaseItemBlacklistEvent(trackedItem);
        Bukkit.getPluginManager().callEvent(databaseItemBlacklistEvent);


    }

    public static List<TrackedItem> getTrackedItems(){
        return null;
    }

    public static Boolean isBlacklisted(TrackedItem item){
        String statement = "SELECT IS_BLACKLISTED FROM TRACKED_ITEMS WHERE UUID = ?";
        Boolean res = null;
        try {
            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, item.getOriginalID().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int blacklisted = resultSet.getInt("IS_BLACKLISTED");
                System.out.println("blacklisted tiny int res : " + blacklisted);
                res = blacklisted != 0;
            } else {
                System.out.println("[Sauron] No data found for UUID: " + item.getOriginalID());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("item is blacklisted ?? " + res);
        return res;
    }

}

