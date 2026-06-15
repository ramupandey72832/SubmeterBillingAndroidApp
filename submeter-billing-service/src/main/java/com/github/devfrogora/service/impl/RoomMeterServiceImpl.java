package com.github.devfrogora.service.impl;

import com.github.devfrogora.data.config.DatabaseConnection;
import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.*;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.dto.RoomDTO;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.dto.SubmeterDTO;
import com.github.devfrogora.service.exception.BusinessRuleException;
import com.github.devfrogora.service.exception.ResourceNotFoundException;
import com.github.devfrogora.service.exception.RoomOccupiedException;
import com.github.devfrogora.service.utils.OperationResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomMeterServiceImpl implements RoomMeterService {

    private final MeterBillingService meterBillingService;

    // Decoupled construction dependency injection pattern
    public RoomMeterServiceImpl(MeterBillingService meterBillingService) {
        this.meterBillingService = meterBillingService;
    }

    @Override
    public OperationResult<String> addRoomWithMeter(String roomNumber, String roomType, String meterSerialNumber, double meterInitialReading) {
        try {
            // 1. Establish atomic transaction boundary
            DatabaseConnection.beginTransaction();

            Optional<Room> existingRoom = DaoManager.getRoomDao().getRoomByNumber(roomNumber);
            if (existingRoom.isPresent()) {
                DatabaseConnection.rollbackTransaction();
                return OperationResult.failure("Room " + roomNumber + " already exists with room id: " + existingRoom.get().getRoomId());
            }

            Room room = new Room();
            room.setRoomNumber(roomNumber);
            room.setRentAmount(roomType.equalsIgnoreCase("2BHK") ? 12000.00 : 7500.00);

            boolean inserted = DaoManager.getRoomDao().insertRoom(room);
            if (!inserted) {
                DatabaseConnection.rollbackTransaction();
                return OperationResult.failure("Failed to provision room asset in the database.");
            }

            int roomId = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Room registration failed to verify."))
                    .getRoomId();

            Optional<Submeter> linkedMeters = DaoManager.getSubmeterDao().getSubmeterByRoomId(roomId);

            if (linkedMeters.isEmpty()) {
                Submeter meter = new Submeter();
                meter.setRoomId(roomId);
                meter.setMeterSerialNumber(meterSerialNumber);
                meter.setInitialReading(meterInitialReading);

                int meterId = DaoManager.getSubmeterDao().insertSubmeter(meter);
                if (meterId < 0) {
                    DatabaseConnection.rollbackTransaction();
                    return OperationResult.failure("Failed to attach hardware submeter registry.");
                }
                meterBillingService.initialMeterReading(meterId, meterInitialReading, 50, 10);
            } else {
                meterBillingService.initialMeterReading(linkedMeters.get().getMeterId(), meterInitialReading, 50, 10);
            }

            // 2. Commit transaction on clean processing execution loops
            DatabaseConnection.commitTransaction();
            return OperationResult.success(roomNumber, "Room and Submeter successfully provisioned.");

        } catch (ResourceNotFoundException e) {
            DatabaseConnection.rollbackTransaction();
            return OperationResult.failure("Verification Failure: " + e.getMessage());
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction();
            return OperationResult.failure("Critical Database error occurred while creating asset structures: " + e.getMessage());
        }
    }

    @Override
    public OperationResult<Void> deleteRoomIfVacant(String roomNumber) {
        try {
            DatabaseConnection.beginTransaction();

            // 1. Check existence
            Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " does not exist."));

            // 2. Structural Constraint Verification
            Optional<Tenancy> activeTenancy = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getRoomId());
            if (activeTenancy.isPresent()) {
                DatabaseConnection.rollbackTransaction();
                // Utilizes your custom RoomOccupiedException message layout parameters cleanly
                RoomOccupiedException exception = new RoomOccupiedException(roomNumber, activeTenancy.get().getTenantId());
                return OperationResult.failure(exception.getMessage());
            }

            // 3. Cleanup associated assets
            Optional<Submeter> linkedMeter = DaoManager.getSubmeterDao().getSubmeterByRoomId(room.getRoomId());
            if (linkedMeter.isPresent()) {
                DaoManager.getSubmeterDao().deleteSubmeter(linkedMeter.get().getMeterId());
            }

            // 4. Safely drop the room asset records
            boolean isDeleted = DaoManager.getRoomDao().deleteRoom(room.getRoomId());
            if (!isDeleted) {
                DatabaseConnection.rollbackTransaction();
                return OperationResult.failure("Database failure occurred while trying to drop Room: " + roomNumber);
            }

            DatabaseConnection.commitTransaction();
            return OperationResult.success(null, "Room " + roomNumber + " and its connected assets dropped successfully.");

        } catch (ResourceNotFoundException e) {
            DatabaseConnection.rollbackTransaction();
            return OperationResult.failure("Lookup Error: " + e.getMessage());
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction();
            return OperationResult.failure("Critical infrastructure storage error on delete execution: " + e.getMessage());
        }
    }

    @Override
    public OperationResult<Void> updateSubmeter(String roomNumber, String oldMeterSerialNumber, String newMeterSerialNumber) {
        try {
            DatabaseConnection.beginTransaction();

            Optional<Submeter> submeterOpt = DaoManager.getSubmeterDao().getSubmeterBySerialNumber(oldMeterSerialNumber);
            if (submeterOpt.isPresent()) {
                Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                        .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " not found."));

                Submeter submeter = submeterOpt.get();
                submeter.setRoomId(room.getRoomId());
                submeter.setMeterSerialNumber(newMeterSerialNumber);
                DaoManager.getSubmeterDao().updateSubmeter(submeter);

                DatabaseConnection.commitTransaction();
                return OperationResult.success(null, "Submeter hardware registration code successfully reassigned.");
            } else {
                DatabaseConnection.rollbackTransaction();
                return OperationResult.failure(new ResourceNotFoundException("Meter " + oldMeterSerialNumber + " not found.").getMessage());
            }

        } catch (ResourceNotFoundException e) {
            DatabaseConnection.rollbackTransaction();
            return OperationResult.failure("Update Target Error: " + e.getMessage());
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction();
            return OperationResult.failure("Storage mutation exception during submeter swapping operations: " + e.getMessage());
        }
    }

    // --- Data Query Methods (Read Operations Return Safe Payload Packages without throw blocks) ---

    @Override
    public List<RoomRegistryDto> getAllRoomReport() {
        List<RoomRegistryDto> registryList = new ArrayList<>();
        try {
            List<Room> rooms = DaoManager.getRoomDao().getAllRooms();

            for (Room room : rooms) {
                Optional<Tenancy> lease = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getRoomId());
                String tenantName = "N/A";
                boolean isVacant = true;

                if (lease.isPresent()) {
                    tenantName = DaoManager.getTenantDao().getTenantById(lease.get().getTenantId())
                            .map(Tenant::getName).orElse("N/A");
                    isVacant = false;
                }

                String serialNumber = DaoManager.getSubmeterDao().getSubmeterByRoomId(room.getRoomId())
                        .map(Submeter::getMeterSerialNumber).orElse("NOT DEPLOYED");

                registryList.add(new RoomRegistryDto(room.getRoomNumber(), tenantName, serialNumber, isVacant));
            }
        } catch (SQLException e) {
            // Logs the explicit trace context internally via BusinessRuleException mapping
            System.err.println(new BusinessRuleException("Error generating room registry report: " + e).getMessage());
            // Returns the processed/empty dataset safely to maintain layout render states without a UI crash
        }
        return registryList;
    }

    @Override
    public Optional<SubmeterDTO> getSubmeterByRoomNumber(String roomNumber) {
        try {
            Optional<Room> roomOpt = DaoManager.getRoomDao().getRoomByNumber(roomNumber);
            if (roomOpt.isEmpty()) {
                return Optional.empty();
            }

            return DaoManager.getSubmeterDao().getSubmeterByRoomId(roomOpt.get().getRoomId())
                    .map(submeter -> new SubmeterDTO(
                            submeter.getMeterId(),
                            submeter.getRoomId(),
                            submeter.getMeterSerialNumber(),
                            submeter.getInitialReading()
                    ));
        } catch (SQLException e) {
            System.err.println("Query Error in getSubmeterByRoomNumber: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<RoomDTO> findByRoomNumber(String roomNumber) {
        try {
            return DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                    .map(room -> new RoomDTO(room.getRoomNumber(), room.getRentAmount()));
        } catch (SQLException e) {
            System.err.println("Query Error in findByRoomNumber: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isRoomExist(String roomNumber) {
        try {
            return DaoManager.getRoomDao().getRoomByNumber(roomNumber).isPresent();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean isRoomVacant(String roomNumber) {
        try {
            Optional<Room> room = DaoManager.getRoomDao().getRoomByNumber(roomNumber);
            if (room.isEmpty()) return true;
            return DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.get().getRoomId()).isEmpty();
        } catch (SQLException e) {
            return true; // Safe fallback configuration on infrastructure drops
        }
    }
}