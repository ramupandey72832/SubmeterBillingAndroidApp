package com.example.rrallutilitylib.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {

    private static DatabaseConfig databaseConfig;
    private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();
    private static Connection sharedReaderConnection = null;

    private DatabaseConnection() {
        // Utility class - prevent instantiation

    }

    public static synchronized void initialize(DatabaseConfig config) throws SQLException {
        if (databaseConfig != null && !databaseConfig.getUrl().equals(config.getUrl())) {
            shutdown();
        }
        databaseConfig = config;
    }

    public static Connection getConnection() throws SQLException {
        if (databaseConfig == null) {
            throw new IllegalStateException("Database Connection has not been initialized yet!");
        }

        Connection transactionalConn = threadConnection.get();
        if (transactionalConn != null && !transactionalConn.isClosed()) {
            return transactionalConn;
        }

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

        if (databaseConfig.getUrl().contains("sqlite") || databaseConfig.getUrl().contains("sqldroid")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
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
                threadConnection.remove();
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
        } catch (SQLException ignored) {
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
            threadConnection.remove();
        }
    }

    public static synchronized void shutdown() {
        try {
            if (sharedReaderConnection != null && !sharedReaderConnection.isClosed()) {
                sharedReaderConnection.close();
            }
        } catch (SQLException ignored) {}
        sharedReaderConnection = null;
    }

    public static synchronized void clearConnection() {
        shutdown();

        Connection threadConn = threadConnection.get();
        try {
            if (threadConn != null && !threadConn.isClosed()) {
                threadConn.close();
            }
        } catch (SQLException ignored) {
        } finally {
            threadConnection.remove();
        }
    }
}