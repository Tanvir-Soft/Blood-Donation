package com.lifelink.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Singleton database connection helper for MySQL.
 */
public class DBConnection {
    private static Connection connection = null;
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static String dbDriver;

    // Static initializer block to load properties and driver class once
    static {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.err.println("Warning: db.properties file not found. Using default configurations.");
                dbUrl = "jdbc:mysql://localhost:3306/lifelink_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                dbUser = "root";
                dbPassword = "1093";
                dbDriver = "com.mysql.cj.jdbc.Driver";
            } else {
                prop.load(input);
                dbUrl = prop.getProperty("db.url");
                dbUser = prop.getProperty("db.user");
                dbPassword = prop.getProperty("db.password");
                dbDriver = prop.getProperty("db.driver");
            }
            
            // Explicitly load the MySQL JDBC driver class
            Class.forName(dbDriver);
            System.out.println("MySQL JDBC Driver loaded successfully.");
        } catch (Exception e) {
            System.err.println("Failed to load db.properties or JDBC Driver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Private constructor to prevent instantiation
    private DBConnection() {}

    /**
     * Retrieves the Singleton Connection instance.
     * Reconnects automatically if the cached connection has been closed.
     *
     * @return Connection instance to MySQL database
     * @throws Exception if connection attempt fails
     */
    public static synchronized Connection getConnection() throws Exception {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("New database connection established successfully.");
        }
        return connection;
    }

    /**
     * Helper method to test the database connection.
     * Useful for checking settings on start-up.
     *
     * @return true if connection is active, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connection test succeeded!");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Closes the active connection if it exists and is open.
     */
    public static synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (Exception e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
