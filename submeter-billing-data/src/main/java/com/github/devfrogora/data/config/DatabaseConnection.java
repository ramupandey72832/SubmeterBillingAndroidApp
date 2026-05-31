package com.github.devfrogora.data.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConfig activeConfig;

    /**
     * The UI layer calls this method exactly once during application startup
     * to inject the chosen database settings.
     */
    public static void initialize(DatabaseConfig config) {
        activeConfig = config;
    }

    public static Connection getConnection() throws SQLException {
        if (activeConfig == null) {
            throw new IllegalStateException("Database Connection has not been initialized by the UI layer yet!");
        }

        // Dynamically load the driver class injected by the UI layer (if provided)
        if (activeConfig.getDriverClassName() != null && !activeConfig.getDriverClassName().isEmpty()) {
            try {
                Class.forName(activeConfig.getDriverClassName());
            } catch (ClassNotFoundException e) {
                throw new SQLException("Failed to find database driver: " + activeConfig.getDriverClassName(), e);
            }
        }

        // Return connection supporting optional credentials
        if (activeConfig.getUsername() != null && !activeConfig.getUsername().isEmpty()) {
            return DriverManager.getConnection(
                    activeConfig.getUrl(),
                    activeConfig.getUsername(),
                    activeConfig.getPassword()
            );
        } else {
            return DriverManager.getConnection(activeConfig.getUrl());
        }
    }
}

//// Inside Desktop UI Startup Logic (e.g., JavaFX Main class)
//
//DatabaseConfig desktopConfig = new DatabaseConfig(
//
//        "jdbc:sqlite:rental_management.db",
//
//        null, // SQLite doesn't require a username
//
//        null, // SQLite doesn't require a password
//
//        "org.sqlite.JDBC"
//
//);
//
//
//
//// Inject into the data layer
//
//DatabaseConnection.initialize(desktopConfig);
//
//
//
//// Inside Android UI Startup Logic (e.g., MainActivity onCreate)
//
//String dbPath = context.getDatabasePath("rental_management.db").getAbsolutePath();
//
//String contextPath = context.getDatabasePath("rental_management.db").getAbsolutePath();
//DatabaseConnection.setDatabaseUrl("jdbc:sqldroid:" + contextPath);
//
//DatabaseConfig androidConfig = new DatabaseConfig(
//
//        "jdbc:sqldroid:" + dbPath,
//
//        null,
//
//        null,
//
//        "org.sqldroid.SQLDroidDriver"
//
//);
//
//
//
//// Inject into the data layer
//
//DatabaseConnection.initialize(androidConfig);
//
//
//
//// Inside a Client UI setting panel connecting to a central office server
//
//DatabaseConfig serverConfig = new DatabaseConfig(
//
//        "jdbc:mysql://192.168.1.50:3306/rental_db",
//
//        "admin",
//
//        "securePassword123",
//
//        "com.mysql.cj.jdbc.Driver"
//
//);
//
//
//
//// Inject into the data layer
//
//DatabaseConnection.initialize(serverConfig);

