package fr.yronusa.ultimatetracker;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import fr.yronusa.ultimatetracker.Event.ItemUpdateDateEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Database {

    public static FileConfiguration config = UltimateTracker.getInstance().getConfig();
    public static String host = config.getString("database.host");
    public static int port = config.getInt("database.port");
    public static String databaseName = config.getString("database.database");
    public static String username = config.getString("database.user");
    public static String password = config.getString("database.password");
    public static String jdbcURL = "jdbc:mysql://" + host + ":" + port + "/" + databaseName +
            "?useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Paris";

    public static MysqlDataSource getDataSource() throws SQLException {
        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setServerName(host);
        dataSource.setPort(port);
        dataSource.setDatabaseName(databaseName);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setServerTimezone(TimeZone.getDefault().getID());

        return dataSource;
    }

    public static void add(TrackedItem trackedItem) {

        System.out.println("ADDING NEW ITEM : " + trackedItem.toString());
        System.out.println("item uuid: " + trackedItem.getOriginalID().toString());

        String itemBase64 = trackedItem.getBase64();
        MysqlDataSource dataSource;

        try {
            dataSource = getDataSource();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
/**
        String base = "INSERT INTO TRACKED_ITEMS (UUID, ITEMBASE64, LAST_UPDATE, LAST_INVENTORIES, IS_BLACKLISTED)\n" +
                "VALUES (" + trackedItem.getOriginalID() + "," + itemBase64 + "," + trackedItem.getLastUpdate() +"," + "trackedItem.getLastInventories().toString()" +", 0);\n";
        **/

        String statement = "INSERT INTO TRACKED_ITEMS (UUID, ITEMBASE64, LAST_UPDATE, LAST_INVENTORIES, IS_BLACKLISTED) VALUES (?, ?, ?, ?, ?)";
        MysqlDataSource finalDataSource = dataSource;
        Bukkit.getScheduler().runTaskAsynchronously(UltimateTracker.getInstance(), new Runnable() {
            @Override
            public void run() {

                Connection conn = null;
                try {
                    conn = finalDataSource.getConnection();
                    PreparedStatement preparedStatement = conn.prepareStatement(statement);
                    preparedStatement.setString(1, trackedItem.getOriginalID().toString());
                    preparedStatement.setString(2, itemBase64);
                    preparedStatement.setTimestamp(3, trackedItem.getLastUpdateItem());
                    preparedStatement.setString(4, "TODO");
                    preparedStatement.setInt(5, 0);
                    System.out.println(preparedStatement);

                    int i = preparedStatement.executeUpdate();
                    if (i > 0) {
                        System.out.println("ROW INSERTED");
                    } else {
                        System.out.println("ROW NOT INSERTED");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }


            }
        });


    }


    public static Timestamp getLastUpdate(UUID uuid){

        MysqlDataSource dataSource;
        try {
            dataSource = getDataSource();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String statement = "SELECT LAST_UPDATE FROM TRACKED_ITEMS WHERE UUID = ?";

        MysqlDataSource finalDataSource = dataSource;
        final Timestamp[] lastUpdateTimestamp = new Timestamp[1];
        Bukkit.getScheduler().runTaskAsynchronously(UltimateTracker.getInstance(), new Runnable() {
            @Override
            public void run() {

                Connection conn;
                try {
                    conn = finalDataSource.getConnection();
                    PreparedStatement preparedStatement = conn.prepareStatement(statement);
                    preparedStatement.setString(1, uuid.toString());
                    System.out.println(preparedStatement);

                    ResultSet resultSet = preparedStatement.executeQuery();

                    // Check if the result set has data
                    if (resultSet.next()) {
                        // Retrieve the last update timestamp from the result set
                        lastUpdateTimestamp[0] = resultSet.getTimestamp("LAST_UPDATE");

                        // Print or use the timestamp as needed
                        System.out.println("Last Update Timestamp for UUID " + uuid.toString() + ": " + lastUpdateTimestamp[0]);
                    } else {
                        System.out.println("No data found for UUID: " + uuid.toString());
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    // Handle the exception appropriately
                }


            }
        });

        return lastUpdateTimestamp[0];
    }
    public static List<InventoryLocation> getLastInventories(UUID uuid){
        MysqlDataSource dataSource = null;
        try {
            dataSource = getDataSource();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String sqlSelectTrackedItem= "SELECT * FROM SAVED_ITEMS WHERE UUID = " + uuid.toString();
        MysqlDataSource finalDataSource = dataSource;

        final String[] res = new String[0];
        Bukkit.getScheduler().runTaskAsynchronously(UltimateTracker.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = finalDataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlSelectTrackedItem);
                    ResultSet rs = ps.executeQuery(); {
                        while (rs.next()) {
                            res[0] = rs.getString("LAST_UPDATE");
                        }


                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    // handle the exception
                }
            }


        });
        return null;
    }
    public static void update(UUID uuid, Timestamp newDate) {

        MysqlDataSource dataSource;
        try {
            dataSource = getDataSource();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String statement = "UPDATE TRACKED_ITEMS SET LAST_UPDATE = ? WHERE UUID = ?";

        MysqlDataSource finalDataSource = dataSource;
        Bukkit.getScheduler().runTaskAsynchronously(UltimateTracker.getInstance(), new Runnable() {
            @Override
            public void run() {

                Connection conn;
                try {
                    conn = finalDataSource.getConnection();
                    PreparedStatement preparedStatement = conn.prepareStatement(statement);
                    preparedStatement.setTimestamp(1, newDate);
                    preparedStatement.setString(2, uuid.toString());
                    System.out.println(preparedStatement);

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

    public static CompletableFuture<Boolean> checkDupli(TrackedItem item) {
        return CompletableFuture.supplyAsync(() -> getLastUpdate(item.getOriginalID()))
                .thenApplyAsync(timestamp -> {
                    if(timestamp.before(item.getLastUpdateItem())){
                        System.out.println("dupli notfound dtb");
                        return false;
                    }

                    else{
                        item.quarantine();
                        System.out.println("dupli found dtb");
                        return true;
                    }

                }, (t) -> Bukkit.getScheduler().runTask(UltimateTracker.getInstance(), t));
    }
}

