package com.submeter.data;


import com.example.rrallutilitylib.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public final class DatabaseMigrator {

    public interface MigrationListener {
        void onMigrationMessage(String message);
        void onMigrationError(String error, Exception e);
    }

    private static MigrationListener migrationListener;

    private DatabaseMigrator() {}

    public static void setMigrationListener(MigrationListener listener) {
        migrationListener = listener;
    }

    public static void migrate() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (Statement stmt = conn.createStatement()) {

            Set<String> columns = new HashSet<>();
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(bills)")) {
                while (rs.next()) {
                    columns.add(rs.getString("name"));
                }
            }

            Set<String> submeterColumns = new HashSet<>();
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(submeters)")) {
                while (rs.next()) {
                    submeterColumns.add(rs.getString("name"));
                }
            }

            if (!submeterColumns.contains("is_active")) {
                stmt.execute("ALTER TABLE submeters ADD COLUMN is_active INTEGER DEFAULT 1;");
            }

            StringBuilder changes = new StringBuilder();

            if (!columns.contains("payment_date")) {
                stmt.execute("ALTER TABLE bills ADD COLUMN payment_date TEXT;");
                changes.append("Added payment_date. ");
            }
            if (!columns.contains("extra_charge")) {
                stmt.execute("ALTER TABLE bills ADD COLUMN extra_charge REAL DEFAULT 0.0;");
                changes.append("Added extra_charge. ");
            }
            if (!columns.contains("fixed_charge")) {
                stmt.execute("ALTER TABLE bills ADD COLUMN fixed_charge REAL DEFAULT 0.0;");
                changes.append("Added fixed_charge. ");
            }
            if (!columns.contains("note")) {
                stmt.execute("ALTER TABLE bills ADD COLUMN note TEXT;");
                changes.append("Added note. ");
            }
            if (!columns.contains("room_number")) {
                stmt.execute("ALTER TABLE bills ADD COLUMN room_number TEXT;");
                changes.append("Added room_number. ");
            }

            if (migrationListener != null) {
                if (changes.length() > 0) {
                    migrationListener.onMigrationMessage("Database updated: " + changes);
                } else {
                    migrationListener.onMigrationMessage("Database schema is up to date.");
                }
            }

        } catch (SQLException e) {
            if (migrationListener != null) {
                migrationListener.onMigrationError("Migration failed", e);
            }
            throw new SQLException("Database migration failed", e);
        }
    }
}

//// 1. Initialize Connection
//DatabaseConnection.initialize(config);
//
//// 2. Run Migration Explicitly
//DatabaseMigrator.setMigrationListener(myListener);
//DatabaseMigrator.migrate();
