package com.submeter.data.dao.impl;

import com.submeter.data.dao.RoomDao;
import com.submeter.data.entities.Room;
import com.example.rrallutilitylib.config.PropertiesLoader;
import com.example.rrallutilitylib.db.JdbcHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SqlLiteRoomDao implements RoomDao {

    @Override
    public boolean insertRoom(Room room) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "room.insert");
        return JdbcHelper.executeUpdate(sql,
                room.getRoomNumber(),
                room.getRoomType()
        );
    }

    @Override
    public Optional<Room> getRoomById(int roomId) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "room.get_by_id");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToRoom, roomId);
    }

    @Override
    public Optional<Room> getRoomByNumber(String roomNumber) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "room.get_by_number");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToRoom, roomNumber);
    }

    @Override
    public List<Room> getAllRooms() throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "room.get_all");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToRoom);
    }


    public List<Room> getVacantRooms()  throws SQLException{
        String sql = PropertiesLoader.getProperty("queries.properties", "room.get_vacant");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToRoom);
    }

    @Override
    public boolean updateRoom(Room room) throws SQLException{
        String sql = PropertiesLoader.getProperty("queries.properties", "room.update");
        return JdbcHelper.executeUpdate(sql,
                room.getRoomNumber(),
                room.getRoomType(),
                room.getId()
        );
    }

    @Override
    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "room.delete");
        return JdbcHelper.executeUpdate(sql, roomId);
    }

    /**
     * Maps a single row from the ResultSet to a Room entity object.
     */
    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomType(rs.getString("room_type"));
        return room;
    }
}