package com.github.devfrogora.data.dao.impl;

import com.github.devfrogora.data.config.SqlLoader;
import com.github.devfrogora.data.dao.MeterReadingDao;
import com.github.devfrogora.data.dao.DbUtils;
import com.github.devfrogora.data.entities.MeterReading;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SQLiteMeterReadingDao implements MeterReadingDao {

    @Override
    public int insertReading(MeterReading reading) throws SQLException {
        String sql = SqlLoader.get("reading.insert");
        return DbUtils.executeInsert(sql,
                reading.getMeterId(),
                reading.getReadingValue(),
                reading.getImageUrlOrPath(),
                reading.getReadingDate()
        );
    }

    @Override
    public Optional<MeterReading> getReadingById(int readingId)  throws SQLException {
        String sql = SqlLoader.get("reading.get_by_id");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToReading, readingId);
    }

    @Override
    public List<MeterReading> getReadingsByMeterId(int meterId) throws SQLException {
        String sql = SqlLoader.get("reading.get_by_meter");
        return DbUtils.executeQueryList(sql, this::mapResultSetToReading, meterId);
    }

    @Override
    public Optional<MeterReading> getLatestReadingByMeterId(int meterId) throws SQLException {
        String sql = SqlLoader.get("reading.get_latest");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToReading, meterId);
    }

    @Override
    public List<MeterReading> getAllReadings() throws SQLException {
        String sql = SqlLoader.get("reading.get_all");
        return DbUtils.executeQueryList(sql, this::mapResultSetToReading);
    }

    @Override
    public boolean updateReading(MeterReading reading)throws SQLException {
        String sql = SqlLoader.get("reading.update");
        return DbUtils.executeUpdate(sql,
                reading.getMeterId(),
                reading.getReadingValue(),
                reading.getImageUrlOrPath(),
                reading.getReadingDate(),
                reading.getReadingId()
        );
    }

    @Override
    public boolean deleteReading(int readingId) throws SQLException {
        String sql = SqlLoader.get("reading.delete");
        return DbUtils.executeUpdate(sql, readingId);
    }

    /**
     * Maps a single row from the ResultSet to a MeterReading entity object.
     */
    private MeterReading mapResultSetToReading(ResultSet rs) throws SQLException {
        MeterReading reading = new MeterReading();
        reading.setReadingId(rs.getInt("reading_id"));
        reading.setMeterId(rs.getInt("meter_id"));
        reading.setReadingValue(rs.getDouble("reading_value"));
        reading.setImageUrlOrPath(rs.getString("image_url_or_path"));
        reading.setReadingDate(rs.getString("reading_date"));
        return reading;
    }
}