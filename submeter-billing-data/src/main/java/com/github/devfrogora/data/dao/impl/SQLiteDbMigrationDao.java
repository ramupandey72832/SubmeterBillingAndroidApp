package com.github.devfrogora.data.dao.impl;

import com.github.devfrogora.data.config.DatabaseConnection;
import com.github.devfrogora.data.dao.DbMigrationDao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDbMigrationDao implements DbMigrationDao {

    @Override
    public boolean executeMigration() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Database connection could not be retrieved from the configuration manager.");
        }

        boolean oldAutoCommit = conn.getAutoCommit();
        try {
            // Disable auto-commit to handle transaction manually
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                // 1. Create the new submeter allocations junction table
                stmt.execute("CREATE TABLE IF NOT EXISTS submeter_allocations (" +
                        "allocation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "meter_id INTEGER NOT NULL, " +
                        "room_id INTEGER NOT NULL, " +
                        "start_date TEXT NOT NULL, " +
                        "end_date TEXT, " +
                        "FOREIGN KEY (meter_id) REFERENCES submeters(meter_id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE);");

                // 2. Migrate existing room relationships into allocations
                // (Only inserts if the allocation doesn't already exist to prevent duplicates)
                stmt.execute("INSERT INTO submeter_allocations (meter_id, room_id, start_date, end_date) " +
                        "SELECT s.meter_id, s.room_id, datetime('now'), NULL " +
                        "FROM submeters s " +
                        "WHERE s.room_id IS NOT NULL " +
                        "AND NOT EXISTS (" +
                        "    SELECT 1 FROM submeter_allocations sa " +
                        "    WHERE sa.meter_id = s.meter_id AND sa.room_id = s.room_id AND sa.end_date IS NULL" +
                        ");");
            }

            // Commit transaction if successful
            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }
}