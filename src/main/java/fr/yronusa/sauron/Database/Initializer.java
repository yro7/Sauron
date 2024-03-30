package fr.yronusa.sauron.Database;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Log;
import fr.yronusa.sauron.Sauron;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static fr.yronusa.sauron.Config.Config.*;
import static fr.yronusa.sauron.Database.Database.getConnection;

public class Initializer {

    public static Connection sqlServerConnection;
    public static String jdbcWithoutName = switch (databaseType) {
        case oracle -> "jdbc:oracle:thin:@" + databaseHost + ":" + databasePort + ":orcl";
        case sqlserver ->
                "jdbc:sqlserver://" + databaseHost + ":" + databasePort + ";databaseName=" ;
        case sqlite -> {
            String path = Sauron.getInstance().getDataFolder().getAbsolutePath();
            yield "jdbc:sqlite:" + path + ".db";
        }
        default -> "jdbc:" + databaseType + "://"
                + databaseHost + ":" + databasePort + "/"+ "?useSSL=false";
    };

    public static Connection getSqlServerConnection() throws SQLException {
        if(!sqlServerConnection.isValid(2)){
            Log.sendMessageAdmin("§c[Sauron] New connection to SQL server.");
            sqlServerConnection = DriverManager.getConnection(jdbcWithoutName, Config.databaseUser,Config.databasePassword);
        }

        return sqlServerConnection;
    }

    /**
     * Proceeds all database-related verification. If a component of the database ie missing, creates it.
     * Connects to the server / then the database if possible.
     * @return true if the database was successfully enabled and can be used in production, false otherwise.
     */

    public static boolean initializeDatabase() {
        try  {

            if(!canConnectToServer()){
                System.out.println("[Sauron] Database connexion failed. Please, check credentials in config file.");
                System.out.println("[Sauron] Database update is OFF, but items will still be tracked according to TrackingRules.");
                return false;
            }

            System.out.println("[Sauron] Successfully connected to the SQL Server.");


            if(!databaseExists()){
                System.out.println("[Sauron] Database do not exists, creating sauron database...");
                createDatabase();
            }

            // Once we have checked that the connection to the SQL server is possible and that the dtb exist, we
            // set up the connection to that database.

            Database.connect();

            if(!tableTrackedItemsExists()){
                System.out.println("[Sauron] Table \"TRACKED_ITEMS\" do not exist, creating it...");
                createTableTrackedItem();
            }

            if(!tableCrashesExists()){
                System.out.println("[Sauron] Table \"CRASHES\" do not exist, creating it..");
                createTableCrashes();
            }
        } catch (SQLException e) {
            System.out.println("[Sauron] Database update is OFF, but items will still be tracked according to TrackingRules.");
            return false;
        }

        try{
            sqlServerConnection.close();
        } catch(Exception e){
            e.printStackTrace();
        }

        initializeCrashesDates();
        System.out.println("crash dates values : " + Database.crashesDates);

        System.out.println("[Sauron] Database ON !");
        return true;
    }

    private static boolean canConnectToServer() throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcWithoutName, Config.databaseUser,Config.databasePassword)) {
            sqlServerConnection = connection;
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static void createTableTrackedItem() throws SQLException {

        String createTableQuery = "CREATE TABLE " + "TRACKED_ITEMS" + " (" +
                "UUID varchar(36) NOT NULL PRIMARY KEY," +
                "ITEMBASE64 longtext," +
                "LAST_UPDATE timestamp," +
                "LAST_INVENTORIES text," +
                "IS_BLACKLISTED tinyint(1)" +
                ")";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(createTableQuery)) {
            preparedStatement.executeUpdate();
        }
    }

    private static boolean tableTrackedItemsExists() throws SQLException {
        DatabaseMetaData metadata = getConnection().getMetaData();
        try (ResultSet resultSet = metadata.getTables(null, null, "TRACKED_ITEMS", null)) {
            return resultSet.next();
        }
    }

    private static void createTableCrashes() throws SQLException {

        String createTableQuery = "CREATE TABLE " + "CRASHES" + " (" +
                "ROLLBACK_TIME TIMESTAMP NOT NULL" +
                "CRASH_TIME TIMESTAMP NOT NULL" +
                ")";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(createTableQuery)) {
            preparedStatement.executeUpdate();
        }
    }

    private static boolean tableCrashesExists() throws SQLException {
        DatabaseMetaData metadata = getConnection().getMetaData();
        try (ResultSet resultSet = metadata.getTables(null, null, "CRASHES", null)) {
            return resultSet.next();
        }
    }
    private static boolean databaseExists() throws SQLException {
        String query = "SHOW DATABASES LIKE '" + databaseName + "'";
        try {
             Statement statement = getSqlServerConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(query);
             return resultSet.next();
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }


    private static void createDatabase() throws SQLException {
        String query = "CREATE DATABASE " + databaseName;
        try {
            PreparedStatement preparedStatement = getSqlServerConnection().prepareStatement(query);
            preparedStatement.executeUpdate();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void initializeCrashesDates(){
        new BukkitRunnable() {
            @Override
            public void run() {
                String statement = "SELECT ROLLBACK_TIME, CRASH_TIME FROM CRASHES";
                List<ImmutablePair<Timestamp,Timestamp>> res = new ArrayList<>();
                try {
                    Connection conn = getConnection();
                    try (PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                Timestamp rollbackTime = resultSet.getTimestamp("ROLLBACK_TIME");
                                Timestamp crashTime = resultSet.getTimestamp("CRASH_TIME");
                                // Process the pair (rollbackTime, crashTime)
                                res.add(new ImmutablePair<>(rollbackTime,crashTime));
                            }
                        }
                    };

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                Database.crashesDates = res;
            }
        }.runTaskAsynchronously(Sauron.getInstance());
    }




}
