package com.github.devfrogora.data.dao;

import com.github.devfrogora.data.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BackupDao {

    public static final String[] TABLES_TO_BACKUP = {
            "rooms",
            "tenants",
            "submeters",
            "tenancies",
            "meter_readings",
            "bills"
    };

    public interface TableDataConsumer {
        void acceptTableMetaData(String tableName, List<String> columnNames) throws Exception;
        void acceptRowData(List<String> rowValues) throws Exception;
        void endTable() throws Exception;
    }

    /**
     * Streams out database table records sequentially into an abstract callback consumer
     */
    public static void exportDatabaseRecords(TableDataConsumer consumer) throws Exception {
        Connection conn = DatabaseConnection.getConnection();

        for (String tableName : TABLES_TO_BACKUP) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                List<String> columns = new ArrayList<>();
                for (int col = 1; col <= columnCount; col++) {
                    columns.add(metaData.getColumnName(col));
                }

                consumer.acceptTableMetaData(tableName, columns);

                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int col = 1; col <= columnCount; col++) {
                        String val = rs.getString(col);
                        row.add(val != null ? val : "");
                    }
                    consumer.acceptRowData(row);
                }
                consumer.endTable();
            }
        }
    }

    public interface TableDataProvider {
        List<String> getColumnsForTable(String tableName);
        int getRowCount(String tableName);
        List<String> getRowAt(String tableName, int rowIndex);
    }

    /**
     * Executes fully isolated transaction block down in the data layer to wipe and overwrite tables
     */
    public static void importDatabaseRecords(TableDataProvider provider) throws Exception {
        Connection conn = DatabaseConnection.getConnection();

        DatabaseConnection.beginTransaction();
        try {
            // 1. Force Disable Foreign Keys (Best effort)
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = OFF;");
            }

            // --- STEP A: DELETE IN REVERSE ORDER (Children first) ---
            // bills -> meter_readings -> tenancies -> submeters -> tenants -> rooms
            for (int i = TABLES_TO_BACKUP.length - 1; i >= 0; i--) {
                String tableName = TABLES_TO_BACKUP[i];
                try (Statement deleteStmt = conn.createStatement()) {
                    deleteStmt.execute("DELETE FROM " + tableName);
                }
            }

            // --- STEP B: INSERT IN FORWARD ORDER (Parents first) ---
            // rooms -> tenants -> submeters -> tenancies -> meter_readings -> bills
            for (String tableName : TABLES_TO_BACKUP) {
                List<String> columns = provider.getColumnsForTable(tableName);
                if (columns == null || columns.isEmpty()) continue;

                int rowCount = provider.getRowCount(tableName);

                StringBuilder columnsBuilder = new StringBuilder();
                StringBuilder placeholdersBuilder = new StringBuilder();
                for (int c = 0; c < columns.size(); c++) {
                    columnsBuilder.append(columns.get(c));
                    placeholdersBuilder.append("?");
                    if (c < columns.size() - 1) {
                        columnsBuilder.append(", ");
                        placeholdersBuilder.append(", ");
                    }
                }

                String insertSql = "INSERT INTO " + tableName + " (" + columnsBuilder + ") VALUES (" + placeholdersBuilder + ");";

                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (int r = 0; r < rowCount; r++) {
                        List<String> rowData = provider.getRowAt(tableName, r);
                        for (int c = 0; c < columns.size(); c++) {
                            String cellContent = rowData.get(c);
                            if (cellContent == null || cellContent.isEmpty()) {
                                pstmt.setNull(c + 1, java.sql.Types.NULL);
                            } else {
                                pstmt.setString(c + 1, cellContent);
                            }
                        }
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }

            // 3. Re-enable Foreign Keys
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }

            DatabaseConnection.commitTransaction();

        } catch (Exception e) {
            DatabaseConnection.rollbackTransaction();
            throw e;
        }
    }
}