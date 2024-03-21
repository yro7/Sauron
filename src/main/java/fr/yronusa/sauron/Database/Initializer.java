package fr.yronusa.sauron.Database;

import fr.yronusa.sauron.Config.Config;
import fr.yronusa.sauron.Log;
import fr.yronusa.sauron.Sauron;

import java.sql.*;

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
            // setup the connection to that database.

            Database.connect();

            if(!tableExists()){
                System.out.println("[Sauron] Table \"TRACKED_ITEMS\" do not exist, creating it...");
                createTable();
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
