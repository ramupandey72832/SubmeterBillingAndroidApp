package com.github.devfrogora.data.config;

public class DatabaseConfig {
    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName; // Optional for newer JDBC, but crucial for cross-platform fallback

    public DatabaseConfig(String url, String username, String password, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
    }

    // Getters
    public String getUrl() { return url; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getDriverClassName() { return driverClassName; }
}