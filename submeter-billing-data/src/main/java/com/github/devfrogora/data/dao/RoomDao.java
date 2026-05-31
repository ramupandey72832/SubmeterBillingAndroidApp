package com.github.devfrogora.data.dao;

import com.github.devfrogora.data.entities.Room;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface RoomDao {
    // Create
    boolean insertRoom(Room room) throws SQLException;

    // Read
    Optional<Room> getRoomById(int roomId) throws SQLException;
    Optional<Room> getRoomByNumber(String roomNumber) throws SQLException;
    List<Room> getAllRooms() throws SQLException;
    List<Room> getVacantRooms() throws SQLException;

    // Update
    boolean updateRoom(Room room) throws SQLException;

    // Delete
    boolean deleteRoom(int roomId) throws SQLException;
}