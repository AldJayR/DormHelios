package com.dormhelios.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties; // For potentially loading from file
import java.io.InputStream; // For loading properties file
import java.io.IOException; // For file loading errors
import java.util.logging.Level;
import java.util.logging.Logger;


public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String PROPERTIES_FILE = "/config/database.properties"; // Path within resources folder

    // --- Configuration Variables ---
    // Load these from the properties file for better practice
    private static String dbUrl = "jdbc:mysql://localhost:3306/dormhelios_db?useSSL=false&serverTimezone=UTC"; // Default URL
    private static String dbUser = "root"; // Default User
    private static String dbPassword = ""; // Default Password
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    // --- Standard JDBC Driver Loading (Without Pooling) ---
    static {
        // Load properties first before trying to load the driver (in case driver class name is in properties)
        loadProperties();
        try {
            Class.forName(JDBC_DRIVER);
            LOGGER.log(Level.INFO, "MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Fatal Error: MySQL JDBC Driver not found in classpath. Ensure the connector JAR is included.", e);
            // If the driver is missing, the application cannot function. Stop it early.
            throw new RuntimeException("Fatal Error: MySQL JDBC Driver not found!", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred during static initialization.", e);
            throw new RuntimeException("Fatal Error: Unexpected error during static initialization.", e);
        }
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                LOGGER.log(Level.WARNING, "Database properties file not found at: " + PROPERTIES_FILE + ". Using default connection values.");
                return;
            }
            props.load(input);

            // Get properties, using defaults if not found in the file
            dbUrl = props.getProperty("db.url", dbUrl);
            dbUser = props.getProperty("db.user", dbUser);
            dbPassword = props.getProperty("db.password", dbPassword);

            LOGGER.log(Level.INFO, "Database properties loaded successfully from " + PROPERTIES_FILE);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading database properties file: " + PROPERTIES_FILE + ". Using default connection values.", e);
        }
    }

    private DatabaseConnection() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Connection getConnection() throws SQLException {

        try {
            Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            if (connection == null) {
                throw new SQLException("Failed to establish database connection - DriverManager returned null.");
            }
            // Optional: Log successful connection acquisition
            // LOGGER.log(Level.FINE, "Database connection obtained successfully.");
            return connection;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection using DriverManager.", e);
            // Log details that might help diagnose (mask password if logging URL)
            LOGGER.log(Level.SEVERE, "Connection URL: " + dbUrl + ", User: " + dbUser);
            throw e; // Re-throw the exception for the DAO layer to handle
        }
    }

}
