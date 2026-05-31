package com.github.devfrogora.data.config;

import com.github.devfrogora.data.dao.DbUtils;

import java.io.InputStream;
import java.util.Properties;

public class SharedSchemaConfigurator {
    private static final String PROPERTIES_FILE = "/database_schema.properties";
    public static void initializeSchema() throws Exception{

        //Loads the schema SQL strings from the properties file.

            // The exact same order executed on whatever platform you are running
            DbUtils.executeUpdate(SqlLoader.get("schema.enable_foreign_keys"));
            DbUtils.executeUpdate(SqlLoader.get("schema.create_rooms_table"));
            DbUtils.executeUpdate(SqlLoader.get("schema.create_submeters_table"));
            DbUtils.executeUpdate(SqlLoader.get("schema.create_tenants_table"));
            DbUtils.executeUpdate(SqlLoader.get("schema.create_tenancies_table"));
            DbUtils.executeUpdate(SqlLoader.get("schema.create_meter_readings_table"));
            DbUtils.executeUpdate(SqlLoader.get("schema.create_bills_table"));
            DbUtils.executeUpdate(SqlLoader.get("schema.create_bill_summaries_view"));

            System.out.println("Shared schema applied successfully.");

    }
}