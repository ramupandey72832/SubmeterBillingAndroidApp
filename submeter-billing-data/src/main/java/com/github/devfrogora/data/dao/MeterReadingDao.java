package com.github.devfrogora.data.dao;

import com.github.devfrogora.data.entities.MeterReading;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface MeterReadingDao {
    // Create
    int insertReading(MeterReading reading) throws SQLException;

    // Read
    Optional<MeterReading> getReadingById(int readingId) throws SQLException;
    List<MeterReading> getReadingsByMeterId(int meterId) throws SQLException;
    Optional<MeterReading> getLatestReadingByMeterId(int meterId) throws SQLException;
    List<MeterReading> getAllReadings() throws SQLException;

    // Update
    boolean updateReading(MeterReading reading) throws SQLException;

    // Delete
    boolean deleteReading(int readingId) throws SQLException;
}