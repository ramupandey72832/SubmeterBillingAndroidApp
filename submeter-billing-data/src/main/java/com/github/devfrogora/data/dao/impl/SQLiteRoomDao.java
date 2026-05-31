package com.github.devfrogora.data.dao.impl;

import com.github.devfrogora.data.config.SqlLoader;
import com.github.devfrogora.data.dao.RoomDao;
import com.github.devfrogora.data.dao.DbUtils;
import com.github.devfrogora.data.entities.Room;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SQLiteRoomDao implements RoomDao {

    @Override
    public boolean insertRoom(Room room) throws SQLException{
        String sql = SqlLoader.get("room.insert");
        return DbUtils.executeUpdate(sql,
                room.getRoomNumber(),
                room.getRoomType(),
                room.getRentAmount()
        );
    }

    @Override
    public Optional<Room> getRoomById(int roomId) throws SQLException {
        String sql = SqlLoader.get("room.get_by_id");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToRoom, roomId);
    }

    @Override
    public Optional<Room> getRoomByNumber(String roomNumber) throws SQLException {
        String sql = SqlLoader.get("room.get_by_number");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToRoom, roomNumber);
    }

    @Override
    public List<Room> getAllRooms() throws SQLException {
        String sql = SqlLoader.get("room.get_all");
        return DbUtils.executeQueryList(sql, this::mapResultSetToRoom);
    }

    @Override
    public List<Room> getVacantRooms()  throws SQLException{
        String sql = SqlLoader.get("room.get_vacant");
        return DbUtils.executeQueryList(sql, this::mapResultSetToRoom);
    }

    @Override
    public boolean updateRoom(Room room) throws SQLException{
        String sql = SqlLoader.get("room.update");
        return DbUtils.executeUpdate(sql,
                room.getRoomNumber(),
                room.getRoomType(),
                room.getRentAmount(),
                room.getRoomId()
        );
    }

    @Override
    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = SqlLoader.get("room.delete");
        return DbUtils.executeUpdate(sql, roomId);
    }

    /**
     * Maps a single row from the ResultSet to a Room entity object.
     */
    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomType(rs.getString("room_type"));
        room.setRentAmount(rs.getDouble("rent_amount"));
        return room;
    }
}