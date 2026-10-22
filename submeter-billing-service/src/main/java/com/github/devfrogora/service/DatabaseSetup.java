package com.github.devfrogora.service;

import com.submeter.data.db.DatabaseInitializer;

public class DatabaseSetup {
    public static void initializeDb(String dburl,String userName,String password,String driverClassName) throws Exception {
        //1. Kill the old connection engine first
        DatabaseInitializer.initializeDb(dburl,userName,password,driverClassName);
    }
}
