package com.github.devfrogora.data.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SqlLoader {
    private static final Properties queries = new Properties();

    static {
        try (InputStream input = SqlLoader.class.getClassLoader().getResourceAsStream("queries.properties")) {
            if (input == null) {
                System.err.println("Warning: Sorry, unable to find queries.properties file.");
            } else {
                queries.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrieves the specific raw SQL string bound to the properties file key.
     */
    public static String get(String key) {
        String sql = queries.getProperty(key);
        if (sql == null) {
            throw new IllegalArgumentException("SQL Query Key mapping not found for: " + key);
        }
        return sql;
    }
}