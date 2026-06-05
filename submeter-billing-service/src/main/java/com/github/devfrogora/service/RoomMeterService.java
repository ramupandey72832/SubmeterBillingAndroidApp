package com.github.devfrogora.service;

import com.github.devfrogora.service.dto.RoomDTO;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface RoomMeterService {
    void addRoomWithMeter(String roomNumber, String roomType, String meterSerialNumber, double meterInitialReading) throws SQLException;
    public boolean isRoomExist(String roomNumber) throws SQLException;
    Optional<RoomDTO> findByRoomNumber(String roomNumber) throws SQLException;

    boolean isRoomVacant(String roomNumber ) throws SQLException ;

    public void deleteRoomIfVacant(String roomNumber) throws SQLException;

    List<RoomRegistryDto> getRoomRegistryReport();
}