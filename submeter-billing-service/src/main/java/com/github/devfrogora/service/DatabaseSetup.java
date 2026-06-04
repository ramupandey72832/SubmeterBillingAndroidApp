package com.github.devfrogora.service;

import com.github.devfrogora.data.config.DatabaseConfig;
import com.github.devfrogora.data.config.DatabaseConnection;
import com.github.devfrogora.data.config.SharedSchemaConfigurator;
import com.github.devfrogora.data.dao.DaoManager;

public class DatabaseSetup {
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
        System.out.println("System: Initializing database connection configurations...");
            DatabaseConfig dbConfig = new DatabaseConfig(
                    dburl,
                    userName,
                    password,
                    driverClassName
            );
            DatabaseConnection.initialize(dbConfig);
            SharedSchemaConfigurator.initializeSchema();
            DaoManager.initialize();
    }
}
