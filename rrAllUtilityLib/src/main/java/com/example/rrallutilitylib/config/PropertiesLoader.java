package com.example.rrallutilitylib.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PropertiesLoader {

    private static final Logger LOGGER = Logger.getLogger(PropertiesLoader.class.getName());

    // Cache properties files so each resource is loaded from disk/classpath only once
    private static final Map<String, Properties> CACHE = new ConcurrentHashMap<>();

    private PropertiesLoader() {
        // Utility class - prevent instantiation
    }

    /**
     * Loads a properties file from the classpath or returns the cached instance if already loaded.
     *
     * @param fileName Name of the properties file (e.g., "queries.properties")
     * @return Loaded Properties object
     */
    public static Properties load(String fileName) {
        return CACHE.computeIfAbsent(fileName, PropertiesLoader::loadFromClasspath);
    }

    /**
     * Retrieves a specific property value from a designated properties file.
     *
     * @param fileName Name of the properties file
     * @param key Property key to look up
     * @return Property value
     * @throws IllegalArgumentException if the key is not found in the file
     */
    public static String getProperty(String fileName, String key) {
        Properties props = load(fileName);
        String value = props.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException(
                    String.format("Property key '%s' not found in file '%s'", key, fileName)
            );
        }
        return value;
    }

    /**
     * Retrieves a property value with a default fallback if the key does not exist.
     */
    public static String getProperty(String fileName, String key, String defaultValue) {
        Properties props = load(fileName);
        return props.getProperty(key, defaultValue);
    }

    private static Properties loadFromClasspath(String fileName) {
        Properties properties = new Properties();
        try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Unable to locate properties file on classpath: {0}", fileName);
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load properties file: " + fileName, ex);
        }
        return properties;
    }
}
