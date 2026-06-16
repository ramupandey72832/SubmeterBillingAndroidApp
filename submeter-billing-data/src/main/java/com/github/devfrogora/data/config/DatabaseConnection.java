package com.github.devfrogora.data.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static DatabaseConfig databaseConfig;

    // ThreadLocal ensures thread-level transaction isolation during multi-statement writes
    private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();

    // Core Fix: Maintain a singular cached reader connection to prevent raw file handle leaks
    private static Connection sharedReaderConnection = null;

    public static synchronized void initialize(DatabaseConfig config) {
        databaseConfig = config;
    }

    /**
     * Safe Client-Side Connection Bridge
     */
    public static Connection getConnection() throws SQLException {
        if (databaseConfig == null) {
            throw new IllegalStateException("Database Connection has not been initialized yet!");
        }

        // 1. If this thread has an active transaction open, reuse it exclusively
        Connection transactionalConn = threadConnection.get();
        if (transactionalConn != null && !transactionalConn.isClosed()) {
            return transactionalConn;
        }

        // 2. Otherwise, route standard read actions through a single thread-safe connection instance
        synchronized (DatabaseConnection.class) {
            if (sharedReaderConnection == null || sharedReaderConnection.isClosed()) {
                sharedReaderConnection = createNewConnectionInstance();
            }
            return sharedReaderConnection;
        }
    }

    private static Connection createNewConnectionInstance() throws SQLException {
        if (databaseConfig.getDriverClassName() != null && !databaseConfig.getDriverClassName().isEmpty()) {
            try {
                Class.forName(databaseConfig.getDriverClassName());
            } catch (ClassNotFoundException e) {
                throw new SQLException("Failed to find database driver: " + databaseConfig.getDriverClassName(), e);
            }
        }

        Connection conn;
        if (databaseConfig.getUsername() != null && !databaseConfig.getUsername().isEmpty()) {
            conn = DriverManager.getConnection(databaseConfig.getUrl(), databaseConfig.getUsername(), databaseConfig.getPassword());
        } else {
            conn = DriverManager.getConnection(databaseConfig.getUrl());
        }

        // CRITICAL ADAPTATION STEP FOR CLIENT SYSTEMS (SQLite/SQLDroid)
        if (databaseConfig.getUrl().contains("sqlite") || databaseConfig.getUrl().contains("sqldroid")) {
            try (Statement stmt = conn.createStatement()) {
                // WAL mode allows simultaneous background reads even during active transactions
                stmt.execute("PRAGMA journal_mode=WAL;");
                // Wait up to 5 seconds for lock releases before throwing an exception
                stmt.execute("PRAGMA busy_timeout=5000;");
            }
        }
        return conn;
    }

    public static void beginTransaction() throws SQLException {
        Connection conn = threadConnection.get();
        if (conn == null || conn.isClosed()) {
            conn = createNewConnectionInstance();
            threadConnection.set(conn);
        }
        conn.setAutoCommit(false);
    }

    public static void commitTransaction() throws SQLException {
        Connection conn = threadConnection.get();
        if (conn != null && !conn.isClosed()) {
            try {
                conn.commit();
                conn.setAutoCommit(true);
            } finally {
                conn.close();
                threadConnection.remove(); // Evict completely from thread cache memory
            }
        }
    }

    public static void rollbackTransaction() {
        Connection conn = threadConnection.get();
        try {
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Suppressed or logged internally
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
            threadConnection.remove(); // Safely clear out the dead thread data map
        }
    }

    /**
     * Call this inside your application's termination lifecycle hooks to release file locks cleanly
     */
    public static synchronized void shutdown() {
        try {
            if (sharedReaderConnection != null && !sharedReaderConnection.isClosed()) {
                sharedReaderConnection.close();
            }
        } catch (SQLException ignored) {}
        sharedReaderConnection = null;
    }
}