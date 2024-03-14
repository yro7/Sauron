package fr.yronusa.ultimatetracker.Database;

import fr.yronusa.ultimatetracker.Config.Config;
import fr.yronusa.ultimatetracker.UltimateTracker;

import java.sql.*;

import static fr.yronusa.ultimatetracker.Config.Config.*;
import static fr.yronusa.ultimatetracker.Database.Database.getConnection;

public class Initializer {

    public static Connection sqlServerConnection;
    public static String jdbcWithoutName = switch (databaseType) {
        case oracle -> "jdbc:oracle:thin:@" + databaseHost + ":" + databasePort + ":orcl";
        case sqlserver ->
                "jdbc:sqlserver://" + databaseHost + ":" + databasePort + ";databaseName=" ;
        case sqlite -> {
            String path = UltimateTracker.getInstance().getDataFolder().getAbsolutePath();
            yield "jdbc:sqlite:" + path + ".db";
        }
        default -> "jdbc:" + databaseType + "://"
                + databaseHost + ":" + databasePort + "/"+ "?useSSL=false";
    };

    public static Connection getSqlServerConnection() throws SQLException {
        if(!sqlServerConnection.isValid(2)){
            UltimateTracker.yro.sendMessage("§C NEW CONNEXION (TO SERVER)");
            sqlServerConnection = DriverManager.getConnection(jdbcWithoutName, Config.databaseUser,Config.databasePassword);
        }

        return sqlServerConnection;
    }

    public static boolean verifyDatabase() {
        try  {

            if(!canConnectToServer()){
                System.out.print("[UltimateTracker] Database connexion failed. Please, check credentials in config file.");
                System.out.print("[UltimateTracker] Database update is OFF, but items will still be tracked according to TrackingRules.");
                return false;
            }

            System.out.print("[UltimateTracker] Successfully connected to the SQL Server.");


            if(!databaseExists()){
                System.out.print("[UltimateTracker] Database do not exists, creating ULTIMATE_TRACKER database...");
                createDatabase();
            }

            // Once we have checked that the connection to the SQL server is possible and that the dtb exist, we
            // setup the connection to that database.

            Database.connect();

            if(!tableExists()){
                System.out.print("[UltimateTracker] Table \"TRACKED_ITEMS\" do not exist, creating it...");
                createTable();
            }
        } catch (SQLException e) {
            System.out.print("[UltimateTracker] Database update is OFF, but items will still be tracked according to TrackingRules.");
            return false;
        }

        System.out.print("[UltimateTracker] Database ON !");
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

    private static void createTable() throws SQLException {

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

    private static boolean tableExists() throws SQLException {
        DatabaseMetaData metadata = getConnection().getMetaData();
        try (ResultSet resultSet = metadata.getTables(null, null, "TRACKED_ITEMS", null)) {
            return resultSet.next();
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




}
