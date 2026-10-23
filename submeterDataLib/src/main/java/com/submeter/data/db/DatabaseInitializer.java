package com.submeter.data.db;

import com.example.rrallutilitylib.db.DatabaseConfig;
import com.example.rrallutilitylib.db.DatabaseConnection;
import com.submeter.data.dao.DaoManager;
import com.submeter.data.db.schema.SchemaInitializer;

public class DatabaseInitializer {
    private static String dbUrl = "jdbc:sqldroid:submeter_bill.db";
    private static String dbUsername = null;
    private static String dbPassword = null;
    private static String driverClassName = "org.sqldroid.SQLDroidDriver";
    public static void initializeDb(
            String androidOrDesktop,
            String dbPath) throws Exception {

        if (androidOrDesktop == null || androidOrDesktop.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Platform must be specified as Android or Desktop."
            );
        }

        System.out.println(
                "System: Initializing database connection configurations..."
        );

        DatabaseConfig dbConfig;

        if ("Desktop".equalsIgnoreCase(androidOrDesktop)) {

            dbConfig = new DatabaseConfig(
                    "jdbc:sqlite:submeter_bill.db",
                    null,
                    null,
                    "org.sqlite.JDBC"
            );

        } else if ("Android".equalsIgnoreCase(androidOrDesktop)) {

            if (dbPath == null || dbPath.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Database path must be provided for Android."
                );
            }

            dbConfig = new DatabaseConfig(
                    "jdbc:sqldroid:" + dbPath,
                    null,
                    null,
                    "org.sqldroid.SQLDroidDriver"
            );

        } else {
            throw new IllegalArgumentException(
                    "Unsupported platform: " + androidOrDesktop
            );
        }

        DatabaseConnection.initialize(dbConfig);

        SchemaInitializer.initializeSchema();

        DaoManager.initialize();
    }

    public static void initializeDb(String dburl,String userName,String password,String driverClassName) throws Exception {
        //1. Kill the old connection engine first
        DatabaseConnection.clearConnection();

        DatabaseInitializer.dbUrl = dburl;
        DatabaseInitializer.dbUsername = userName;
        DatabaseInitializer.dbPassword = password;
        DatabaseInitializer.driverClassName = driverClassName;
        System.out.println("System: Initializing database connection configurations...");
        DatabaseConfig dbConfig = new DatabaseConfig(
                DatabaseInitializer.dbUrl,
                DatabaseInitializer.dbUsername,
                DatabaseInitializer.dbPassword,
                DatabaseInitializer.driverClassName
        );
        DatabaseConnection.initialize(dbConfig);
        SchemaInitializer.initializeSchema();
        DaoManager.initialize();
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static void setDbUrl(String dbUrl) {
        DatabaseInitializer.dbUrl = dbUrl;
    }

    public static String getDbUsername() {
        return dbUsername;
    }

    public static void setDbUsername(String dbUsername) {
        DatabaseInitializer.dbUsername = dbUsername;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static void setDbPassword(String dbPassword) {
        DatabaseInitializer.dbPassword = dbPassword;
    }

    public static String getDriverClassName() {
        return driverClassName;
    }

    public static void setDriverClassName(String driverClassName) {
        DatabaseInitializer.driverClassName = driverClassName;
    }
}
