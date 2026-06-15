package com.github.devfrogora.data.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConfig activeConfig;

    // A ThreadLocal wrapper ensures that every execution thread receives its own
    // private transaction stream safely without thread racing or connection collision.
    private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();

    /**
     * The UI layer calls this method exactly once during application startup
     * to inject the chosen database settings.
     */
    public static synchronized void initialize(DatabaseConfig config) {
        activeConfig = config;
    }

    /**
     * Retrieves the database connection allocated to the current running thread.
     * If a transaction is active, it returns the transaction connection wrapper.
     */
    public static Connection getConnection() throws SQLException {
        if (activeConfig == null) {
            throw new IllegalStateException("Database Connection has not been initialized by the UI layer yet!");
        }

        // If this thread already has an active, transactional connection, reuse it!
        Connection conn = threadConnection.get();
        if (conn != null && !conn.isClosed()) {
            return conn;
        }

        // Otherwise, spawn a fresh, non-transactional standalone connection
        return createNewConnectionInstance();
    }

    private static Connection createNewConnectionInstance() throws SQLException {
        if (activeConfig.getDriverClassName() != null && !activeConfig.getDriverClassName().isEmpty()) {
            try {
                Class.forName(activeConfig.getDriverClassName());
            } catch (ClassNotFoundException e) {
                throw new SQLException("Failed to find database driver: " + activeConfig.getDriverClassName(), e);
            }
        }

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

    /**
     * Starts an atomic transaction scope strictly tied to the calling execution thread.
     */
    public static void beginTransaction() throws SQLException {
        Connection conn = threadConnection.get();
        if (conn == null || conn.isClosed()) {
            conn = createNewConnectionInstance();
            threadConnection.set(conn);
        }
        conn.setAutoCommit(false); // Disables SQLite immediate auto-save processing
    }

    /**
     * Commits all modifications performed inside the current thread's transaction scope.
     */
    public static void commitTransaction() throws SQLException {
        Connection conn = threadConnection.get();
        if (conn != null && !conn.isClosed()) {
            try {
                conn.commit();
                conn.setAutoCommit(true);
            } finally {
                conn.close(); // Clean up hardware references back to OS limits
                threadConnection.remove(); // Evict completely from Thread memory
            }
        }
    }

    /**
     * Discards any pending mutations safely if a business rule fails or crashes mid-flight.
     */
    public static void rollbackTransaction() {
        Connection conn = threadConnection.get();
        try {
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Log rollback failure internally if required
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
            threadConnection.remove(); // Safely clear out the dead thread data map
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

