package com.github.devfrogora.service;

import com.github.devfrogora.data.config.DatabaseConfig;
import com.github.devfrogora.data.config.DatabaseConnection;
import com.github.devfrogora.data.config.SharedSchemaConfigurator;
import com.github.devfrogora.data.dao.DaoManager;

public class DatabaseSetup {
    private static String dbUrl = "jdbc:sqldroid:submeter_bill.db";
    private static String dbUsername = null;
    private static String dbPassword = null;
    private static String driverClassName = "org.sqldroid.SQLDroidDriver";
    public static void initializeDb(String AndroidOrDesktop,String dbPath) throws Exception {
        System.out.println("System: Initializing database connection configurations...");
        if(AndroidOrDesktop.equalsIgnoreCase("Desktop")){
            DatabaseConfig desktopConfig = new DatabaseConfig(
                    "jdbc:sqlite:submeter_bill.db",
                    null,
                    null,
                    "org.sqlite.JDBC"
            );
            DatabaseConnection.initialize(desktopConfig);
            SharedSchemaConfigurator.initializeSchema();
            DaoManager.initialize();
        }else{
//            String dbPath = getFilesDir().getAbsolutePath() + "/submeter_bill.db";
            DatabaseConfig androidConfig = new DatabaseConfig(
                    "jdbc:sqldroid:" + dbPath,
                    null,
                    null,
                    "org.sqldroid.SQLDroidDriver"
            );
            DatabaseConnection.initialize(androidConfig);

        }
        SharedSchemaConfigurator.initializeSchema();
        DaoManager.initialize();
    }

    public static void initializeDb(String dburl,String userName,String password,String driverClassName) throws Exception {
        //1. Kill the old connection engine first
        DatabaseConnection.clearConnection();

        DatabaseSetup.dbUrl = dburl;
        DatabaseSetup.dbUsername = userName;
        DatabaseSetup.dbPassword = password;
        DatabaseSetup.driverClassName = driverClassName;
        System.out.println("System: Initializing database connection configurations...");
            DatabaseConfig dbConfig = new DatabaseConfig(
                    DatabaseSetup.dbUrl,
                    DatabaseSetup.dbUsername,
                    DatabaseSetup.dbPassword,
                    DatabaseSetup.driverClassName
            );
            DatabaseConnection.initialize(dbConfig);
            SharedSchemaConfigurator.initializeSchema();
            DaoManager.initialize();
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static void setDbUrl(String dbUrl) {
        DatabaseSetup.dbUrl = dbUrl;
    }

    public static String getDbUsername() {
        return dbUsername;
    }

    public static void setDbUsername(String dbUsername) {
        DatabaseSetup.dbUsername = dbUsername;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static void setDbPassword(String dbPassword) {
        DatabaseSetup.dbPassword = dbPassword;
    }

    public static String getDriverClassName() {
        return driverClassName;
    }

    public static void setDriverClassName(String driverClassName) {
        DatabaseSetup.driverClassName = driverClassName;
    }
}
