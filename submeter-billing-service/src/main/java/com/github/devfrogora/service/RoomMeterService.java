package com.github.devfrogora.service;

import com.github.devfrogora.service.dto.RoomDTO;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.dto.SubmeterDTO;

import java.util.List;
import java.util.Optional;

import com.github.devfrogora.service.utils.OperationResult;


public interface RoomMeterService {

    /**
     * Provisions a room asset and attaches a submeter to it atomically.
     * Reports an OperationResult containing the verified room number back to the ViewModel.
     */
    OperationResult<String> addRoomWithMeter(String roomNumber, String roomType, String meterSerialNumber, double meterInitialReading);

    /**
     * Modifies the hardware submeter serial numbers.
     * Reports an OperationResult confirming execution status back to the ViewModel.
     */
    OperationResult<Void> updateSubmeter(String roomNumber, String oldMeterSerialNumber, String newMeterSerialNumber,double initialReading);

    /**
     * Safely tears down room assets if they are currently unrented.
     * Reports an OperationResult confirming cleanup status back to the ViewModel.
     */
    OperationResult<Void> deleteRoomIfVacant(String roomNumber);

    /**
     * Generates a structural room report matrix.
     * Pure read query: exceptions are caught internally and logged/handled, returning a safe list.
     */
    OperationResult<List<RoomRegistryDto>> getAllRoomReport();
    OperationResult<List<String>> getAllRoomNumbers();
    /**
     * Queries for a dynamic submeter payload map.
     * Returns an empty Optional if the target database pointer is missing.
     */
    OperationResult<SubmeterDTO> getSubmeterByRoomNumber(String roomNumber);

    /**
     * Checks if a target room asset is tracked on the disk.
     */
    boolean isRoomExist(String roomNumber);

    /**
     * Locates a mapped Room model configuration.
     */
    OperationResult<RoomDTO> findByRoomNumber(String roomNumber);

    /**
     * Verifies lease status flags across active constraints.
     */
    boolean isRoomVacant(String roomNumber);
}