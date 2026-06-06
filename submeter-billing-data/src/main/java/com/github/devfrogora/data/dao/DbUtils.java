package com.github.devfrogora.data.dao;

import com.github.devfrogora.data.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DbUtils {
    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public static <T> Optional<T> executeQuerySingle(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException{
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public static <T> List<T> executeQueryList(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException{
        List<T> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
            }
        }
        return list;
    }

    public static boolean executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Loop through parameters and dynamically bind them
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]); // JDBC automatically handles types like Int, Double, String
            }

            return pstmt.executeUpdate() > 0;
        }
    }

    public static int executeInsertArchieved(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Dynamically bind parameters to placeholders
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Fetch the generated keys result set from the driver
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the newly created auto-incremented primary key
                    }
                }
            }
        }
        return -1; // Return -1 if the operation fails
    }

    public static int executeInsert(String sql, Object... params) throws SQLException {
        // 1. Get connection and ensure it's not null
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Database connection could not be established (returned null).");
            }

            // 2. Fall back to a standard prepareStatement if the driver chokes on RETURN_GENERATED_KEYS
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                if (pstmt == null) {
                    throw new SQLException("Failed to prepare statement. Driver returned null for SQL: " + sql);
                }

                // Dynamically bind parameters to placeholders
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        pstmt.setObject(i + 1, params[i]);
                    }
                }

                int affectedRows = pstmt.executeUpdate();

                // 3. If rows were inserted, query SQLite for the last generated ID
                if (affectedRows > 0) {
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid();")) {
                        if (rs.next()) {
                            return rs.getInt(1); // Returns the newly created auto-incremented primary key
                        }
                    }
                }
            }
        }
        return -1; // Return -1 if the operation fails
    }

}