package com.github.devfrogora.data.dao.impl;

import com.github.devfrogora.data.config.SqlLoader;
import com.github.devfrogora.data.dao.SubmeterDao;
import com.github.devfrogora.data.dao.DbUtils;
import com.github.devfrogora.data.entities.Submeter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SQLiteSubmeterDao implements SubmeterDao {

    @Override
    public int insertSubmeter(Submeter submeter) throws SQLException {
        // Room ID is excluded here because submeters are pure hardware assets
        String sql = SqlLoader.get("submeter.insert");
        return DbUtils.executeInsert(sql,
                submeter.getMeterSerialNumber(),
                submeter.getInitialReading(),
                submeter.getIsActive()
        );
    }

    @Override
    public Optional<Submeter> getSubmeterById(int meterId) throws SQLException {
        String sql = SqlLoader.get("submeter.get_by_id");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToSubmeter, meterId);
    }

    @Override
    public Optional<Submeter> getSubmeterBySerialNumber(String serialNumber) throws SQLException {
        String sql = SqlLoader.get("submeter.get_by_serial");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToSubmeter, serialNumber);
    }

    @Override
    public Optional<Submeter> getSubmeterByRoomId(int roomId) throws SQLException {
        // Fetches the submeter linked to a room via the allocation table join
        String sql = SqlLoader.get("submeter.get_by_room_join");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToSubmeter, roomId);
    }

    @Override
    public Optional<Submeter> getInactiveSubmeterByRoomId(int roomId) throws SQLException {
        String sql = SqlLoader.get("submeter.get_inactive_by_room_join");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToSubmeter, roomId);
    }

    @Override
    public boolean detachSubmeter(int meterId) throws SQLException {
        // Handled via allocation update (e.g., closing the allocation record) rather than updating submeters table
        String sql = SqlLoader.get("submeter.detach");
        return DbUtils.executeUpdate(sql, meterId);
    }

    @Override
    public boolean deactivateSubmeter(int meterId) throws SQLException {
        String sql = "UPDATE submeters SET is_active = 0 WHERE meter_id = ?";
        return DbUtils.executeUpdate(sql, meterId);
    }

    @Override
    public List<Submeter> getAllSubmeters() throws SQLException {
        String sql = SqlLoader.get("submeter.get_all");
        return DbUtils.executeQueryList(sql, this::mapResultSetToSubmeter);
    }

    @Override
    public boolean updateSubmeter(Submeter submeter) throws SQLException {
        // Room ID is no longer updated here
        String sql = SqlLoader.get("submeter.update");
        return DbUtils.executeUpdate(sql,
                submeter.getMeterSerialNumber(),
                submeter.getInitialReading(),
                submeter.getIsActive(),
                submeter.getMeterId()
        );
    }

    @Override
    public boolean deleteSubmeter(int meterId) throws SQLException {
        String sql = SqlLoader.get("submeter.delete");
        return DbUtils.executeUpdate(sql, meterId);
    }

    /**
     * Maps a single row from the ResultSet to a Submeter entity object (without room_id).
     */
    private Submeter mapResultSetToSubmeter(ResultSet rs) throws SQLException {
        Submeter submeter = new Submeter();
        submeter.setMeterId(rs.getInt("meter_id"));
        submeter.setMeterSerialNumber(rs.getString("meter_serial_number"));
        submeter.setInitialReading(rs.getDouble("initial_reading"));
        submeter.setIsActive(rs.getInt("is_active"));
        return submeter;
    }
}