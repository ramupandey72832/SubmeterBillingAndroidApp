package com.github.devfrogora.service.impl;

import com.github.devfrogora.data.config.DatabaseConnection;
import com.github.devfrogora.data.dao.DaoManager;
//import com.github.devfrogora.data.entities.Submeter;
//import com.github.devfrogora.data.entities.Tenancy;
//import com.github.devfrogora.data.entities.Tenant;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.dto.RoomDTO;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.dto.SubmeterDTO;
import com.github.devfrogora.service.exception.ResourceNotFoundException;
import com.github.devfrogora.service.exception.RoomOccupiedException;
import com.github.devfrogora.service.utils.OperationResult;
import com.submeter.data.entities.Room;
import com.submeter.data.entities.Submeter;
import com.submeter.data.entities.Tenancy;
import com.submeter.data.entities.Tenant;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoomMeterServiceImpl implements RoomMeterService {

    private final MeterBillingService meterBillingService;

    public RoomMeterServiceImpl(MeterBillingService meterBillingService) {
        this.meterBillingService = meterBillingService;
    }

    @Override
    public OperationResult<String> addRoomWithMeter(String roomNumber, String roomType, String meterSerialNumber, long meterInitialReading) {
        try {
            DatabaseConnection.beginTransaction();

            // 1. Check if room exists
            if (DaoManager.getRoomDao().getRoomByNumber(roomNumber).isPresent()) {
                DatabaseConnection.rollbackTransaction();
                return OperationResult.failure("Room " + roomNumber + " already exists.");
            }

            // 2. Insert Room
            Room room = new Room();
            room.setRoomNumber(roomNumber);
            room.setRoomType(roomType);

            if (!DaoManager.getRoomDao().insertRoom(room)) {
                DatabaseConnection.rollbackTransaction();
                return OperationResult.failure("Failed to provision room asset.");
            }

            // 3. Get Room ID
            int roomId = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found after insert."))
                    .getId();

            // 4. Handle Submeter
            Optional<Submeter> existingMeter = DaoManager.getSubmeterDao().getSubmeterByRoomId(roomId);
            if (existingMeter.isEmpty()) {
                Submeter meter = new Submeter();
                meter.setMeterSerialNumber(meterSerialNumber);
                meter.setInitialReading(meterInitialReading);

                int meterId = DaoManager.getSubmeterDao().insertSubmeter(meter);
                if (meterId < 0) {
                    DatabaseConnection.rollbackTransaction();
                    return OperationResult.failure("Failed to attach submeter.");
                }
                meterBillingService.initialMeterReading(meterId, meterInitialReading, 0, 0);
            } else {
                meterBillingService.initialMeterReading(existingMeter.get().getId(), meterInitialReading, 50, 10);
            }

            DatabaseConnection.commitTransaction();
            return OperationResult.success(roomNumber, "Room and Submeter successfully provisioned.");

        } catch (Exception e) {
            DatabaseConnection.rollbackTransaction();
            return OperationResult.failure("Error: " + e.getMessage());
        }
    }

    @Override
    public OperationResult<Void> deleteRoomIfVacant(String roomNumber) {
        try {
            DatabaseConnection.beginTransaction();

            com.submeter.data.entities.Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " does not exist."));

            Optional<Tenancy> activeTenancy = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getId());
            if (activeTenancy.isPresent()) {
                DatabaseConnection.rollbackTransaction();
                RoomOccupiedException ex = new RoomOccupiedException(roomNumber, activeTenancy.get().getTenantId());
                return OperationResult.failure(ex.getMessage());
            }

            DaoManager.getSubmeterDao().getSubmeterByRoomId(room.getId())
                    .ifPresent(meter -> {
                        try {
                            DaoManager.getSubmeterDao().deactivateSubmeter(meter.getId());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            if (!DaoManager.getRoomDao().deleteRoom(room.getId())) {
                DatabaseConnection.rollbackTransaction();
                return OperationResult.failure("Failed to delete room.");
            }

            DatabaseConnection.commitTransaction();
            return OperationResult.success(null, "Room dropped successfully.");

        } catch (Exception e) {
            DatabaseConnection.rollbackTransaction();
            return OperationResult.failure("Error: " + e.getMessage());
        }
    }

    @Override
    public OperationResult<Void> updateSubmeter(String roomNumber, String oldMeterSerialNumber, String newMeterSerialNumber, long initialReading) {
        try {
            DatabaseConnection.beginTransaction();

            Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found."));

            // Deactivate old meter if present
            DaoManager.getSubmeterDao().getSubmeterByRoomId(room.getId())
                    .ifPresent(old -> {
                        try {
                            DaoManager.getSubmeterDao().deactivateSubmeter(old.getId());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            // Setup new meter
            Optional<Submeter> existingMeter = DaoManager.getSubmeterDao().getSubmeterBySerialNumber(newMeterSerialNumber);
            int meterId;

            if (existingMeter.isPresent()) {
                Submeter meter = existingMeter.get();
                meter.setInitialReading(initialReading);
                DaoManager.getSubmeterDao().updateSubmeter(meter);
                meterId = meter.getId();
            } else {
                Submeter meter = new Submeter();
                meter.setMeterSerialNumber(newMeterSerialNumber);
                meter.setInitialReading(initialReading);
                meterId = DaoManager.getSubmeterDao().insertSubmeter(meter);
            }

            if (meterId < 0) {
                DatabaseConnection.rollbackTransaction();
                return OperationResult.failure("Failed to save submeter hardware.");
            }

            meterBillingService.initialMeterReading(meterId, initialReading, 0, 0);

            DatabaseConnection.commitTransaction();
            return OperationResult.success(null, "Hardware replaced successfully.");

        } catch (Exception e) {
            DatabaseConnection.rollbackTransaction();
            return OperationResult.failure("Hardware swap failed: " + e.getMessage());
        }
    }

    // --- Query Methods (Read-Only) ---

    @Override
    public OperationResult<List<RoomRegistryDto>> getAllRoomReport() {
        try {
            List<RoomRegistryDto> list = new ArrayList<>();
            List<Room> rooms = DaoManager.getRoomDao().getAllRooms();

            for (Room room : rooms) {
                Optional<Tenancy> lease = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getId());
                String tenantName = lease.flatMap(t -> {
                            try {
                                return DaoManager.getTenantDao().getTenantById(t.getTenantId());
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .map(Tenant::getName).orElse("N/A");
                String serialNumber = DaoManager.getSubmeterDao().getSubmeterByRoomId(room.getId())
                        .map(Submeter::getMeterSerialNumber).orElse("NOT DEPLOYED");

                list.add(new RoomRegistryDto(room.getRoomNumber(), tenantName, serialNumber, lease.isEmpty()));
            }
            return OperationResult.success(list, "Report loaded successfully.");
        } catch (SQLException e) {
            return OperationResult.failure("Failed to load report: " + e.getMessage());
        }
    }

    @Override
    public OperationResult<List<String>> getAllRoomNumbers() {
        try {
            List<String> numbers = DaoManager.getRoomDao().getAllRooms().stream()
                    .map(Room::getRoomNumber)
                    .collect(Collectors.toList());
            return OperationResult.success(numbers, "Rooms loaded.");
        } catch (Exception e) {
            return OperationResult.failure("Failed: " + e.getMessage());
        }
    }

    @Override
    public OperationResult<SubmeterDTO> getSubmeterByRoomNumber(String roomNumber) {
        try {
            Optional<Room> room = DaoManager.getRoomDao().getRoomByNumber(roomNumber);
            if (room.isEmpty()) return OperationResult.failure("Room not found.");

            return DaoManager.getSubmeterDao().getSubmeterByRoomId(room.get().getId())
                    .map(sub -> OperationResult.success(new SubmeterDTO(sub.getId(), sub.getMeterSerialNumber(), sub.getInitialReading()), "Found."))
                    .orElse(OperationResult.failure("No submeter attached."));
        } catch (SQLException e) {
            return OperationResult.failure("Error: " + e.getMessage());
        }
    }

    @Override
    public OperationResult<RoomDTO> findByRoomNumber(String roomNumber) {
        try {
            return DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                    .map(r -> OperationResult.success(new RoomDTO(r.getRoomNumber(), r.getRoomType()), "Found."))
                    .orElse(OperationResult.failure("Room not found."));
        } catch (SQLException e) {
            return OperationResult.failure("Error: " + e.getMessage());
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
            return DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.get().getId()).isEmpty();
        } catch (SQLException e) {
            return true;
        }
    }
}