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
    public int insertSubmeter(Submeter submeter) throws SQLException{
        String sql = SqlLoader.get("submeter.insert");
        return DbUtils.executeInsert(sql,
                submeter.getRoomId(),
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
    public Optional<Submeter> getSubmeterBySerialNumber(String serialNumber)  throws SQLException{
        String sql = SqlLoader.get("submeter.get_by_serial");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToSubmeter, serialNumber);
    }

    @Override
    public Optional<Submeter> getSubmeterByRoomId(int roomId) throws SQLException{
        String sql = SqlLoader.get("submeter.get_by_room");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToSubmeter, roomId);
    }

    @Override
    public boolean detachSubmeter(int meterId) throws SQLException {
        // We set room_id to null (or -1 if your schema doesn't allow nulls)
        // to "archive" the meter hardware while keeping its ID for old bills.
        String sql = "UPDATE submeters SET room_id = NULL WHERE meter_id = ?";
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
        String sql = SqlLoader.get("submeter.update");
        return DbUtils.executeUpdate(sql,
                submeter.getRoomId(),
                submeter.getMeterSerialNumber(),
                submeter.getInitialReading(),
                submeter.getIsActive(),
                submeter.getMeterId()
        );
    }

    @Override
    public boolean deleteSubmeter(int meterId) throws SQLException{
        String sql = SqlLoader.get("submeter.delete");
        return DbUtils.executeUpdate(sql, meterId);
    }

    /**
     * Maps a single row from the ResultSet to a Submeter entity object.
     */
    private Submeter mapResultSetToSubmeter(ResultSet rs) throws SQLException {
        Submeter submeter = new Submeter();
        submeter.setMeterId(rs.getInt("meter_id"));
        submeter.setRoomId(rs.getInt("room_id"));
        submeter.setMeterSerialNumber(rs.getString("meter_serial_number"));
        submeter.setInitialReading(rs.getDouble("initial_reading"));
        submeter.setIsActive(rs.getInt("is_active"));
        return submeter;
    }
}