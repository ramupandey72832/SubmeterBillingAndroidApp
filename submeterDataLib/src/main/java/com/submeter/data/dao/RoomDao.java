package com.submeter.data.dao;

import com.submeter.data.entities.Room;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface RoomDao {
    boolean insertRoom(Room room) throws SQLException;
    // Read
    Optional<Room> getRoomById(int roomId) throws SQLException;
    Optional<Room> getRoomByNumber(String roomNumber) throws SQLException;
    List<Room> getAllRooms() throws SQLException;

    // Update
    boolean updateRoom(Room room) throws SQLException;

    // Delete
    boolean deleteRoom(int roomId) throws SQLException;
}
