package com.submeter.data.dao.impl;

import com.submeter.data.dao.MeterReadingDao;
import com.submeter.data.entities.MeterReading;
import com.example.rrallutilitylib.config.PropertiesLoader;
import com.example.rrallutilitylib.db.JdbcHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SqlLiteMeterReadingDao implements MeterReadingDao {
    @Override
    public int insertReading(MeterReading reading) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties","reading.insert");
        return JdbcHelper.executeInsert(sql,
                reading.getMeterId(),
                reading.getReadingValue(),
                reading.getImageUrlOrPath(),
                reading.getDate()
        );
    }

    @Override
    public Optional<MeterReading> getReadingById(Integer readingId)  throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties","reading.get_by_id");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToReading, readingId);
    }

    @Override
    public List<MeterReading> getReadingsByMeterId(int meterId) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties","reading.get_by_meter");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToReading, meterId);
    }

    @Override
    public Optional<MeterReading> getLatestReadingByMeterId(int meterId) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties","reading.get_latest");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToReading, meterId);
    }

    @Override
    public Optional<MeterReading> getLatestMeterReadingBySerialNumber(String serialNumber) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties","reading.get_latest_by_serial");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToReading, serialNumber);
    }

    @Override
    public List<MeterReading> getAllReadings() throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties","reading.get_all");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToReading);
    }

    @Override
    public boolean updateReading(MeterReading reading)throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties","reading.update");
        return JdbcHelper.executeUpdate(sql,
                reading.getMeterId(),
                reading.getReadingValue(),
                reading.getImageUrlOrPath(),
                reading.getDate(),
                reading.getId()
        );
    }

    @Override
    public boolean deleteReading(int readingId) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties","reading.delete");
        return JdbcHelper.executeUpdate(sql, readingId);
    }

    /**
     * Maps a single row from the ResultSet to a MeterReading entity object.
     */
    private MeterReading mapResultSetToReading(ResultSet rs) throws SQLException {
        MeterReading reading = new MeterReading();
        reading.setId(rs.getInt("reading_id"));
        reading.setMeterId(rs.getInt("meter_id"));
        reading.setReadingValue(rs.getLong("reading_value"));
        reading.setImageUrlOrPath(rs.getString("image_url_or_path"));
        reading.setDate(rs.getString("reading_date"));
        return reading;
    }
}