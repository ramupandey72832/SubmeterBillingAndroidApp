package com.submeter.data.dao.impl;

import com.submeter.data.dao.SubmeterDao;
import com.submeter.data.entities.Submeter;
import com.example.rrallutilitylib.config.PropertiesLoader;
import com.example.rrallutilitylib.db.JdbcHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SqlLiteSubmeterDao implements SubmeterDao {
    @Override
    public int insertSubmeter(Submeter submeter) throws SQLException {
        // Room ID is excluded here because submeters are pure hardware assets
        String sql = PropertiesLoader.getProperty("queries.properties", "submeter.insert");
        return JdbcHelper.executeInsert(sql,
                submeter.getMeterSerialNumber(),
                submeter.getInitialReading()
        );
    }

    @Override
    public Optional<Submeter> getSubmeterById(int meterId) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "submeter.get_by_id");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToSubmeter, meterId);
    }

    @Override
    public Optional<Submeter> getSubmeterBySerialNumber(String serialNumber) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "submeter.get_by_serial");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToSubmeter, serialNumber);
    }

    @Override
    public Optional<Submeter> getSubmeterByRoomId(int roomId) throws SQLException {
        // Fetches the submeter linked to a room via the allocation table join
        String sql = PropertiesLoader.getProperty("queries.properties", "submeter.get_by_room_join");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToSubmeter, roomId);
    }

    @Override
    public Optional<Submeter> getInactiveSubmeterByRoomId(int roomId) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "submeter.get_inactive_by_room_join");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToSubmeter, roomId);
    }

    @Override
    public boolean detachSubmeter(int meterId) throws SQLException {
        // Handled via allocation update (e.g., closing the allocation record) rather than updating submeters table
        String sql = PropertiesLoader.getProperty("queries.properties", "submeter.detach");
        return JdbcHelper.executeUpdate(sql, meterId);
    }

    @Override
    public boolean deactivateSubmeter(int meterId) throws SQLException {
        String sql = "UPDATE submeters SET is_active = 0 WHERE meter_id = ?";
        return JdbcHelper.executeUpdate(sql, meterId);
    }

    @Override
    public List<Submeter> getAllSubmeters() throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "submeter.get_all");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToSubmeter);
    }

    @Override
    public boolean updateSubmeter(Submeter submeter) throws SQLException {
        // Room ID is no longer updated here
        String sql = PropertiesLoader.getProperty("queries.properties", "submeter.update");
        return JdbcHelper.executeUpdate(sql,
                submeter.getMeterSerialNumber(),
                submeter.getInitialReading(),
                submeter.getId()
        );
    }

    @Override
    public boolean deleteSubmeter(int meterId) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "submeter.delete");
        return JdbcHelper.executeUpdate(sql, meterId);
    }

    /**
     * Maps a single row from the ResultSet to a Submeter entity object (without room_id).
     */
    private Submeter mapResultSetToSubmeter(ResultSet rs) throws SQLException {
        Submeter submeter = new Submeter();
        submeter.setId(rs.getInt("meter_id"));
        submeter.setMeterSerialNumber(rs.getString("meter_serial_number"));
        submeter.setInitialReading(rs.getLong("initial_reading"));
        return submeter;
    }
}
