package com.github.devfrogora.service.impl;

import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.*;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.dto.RoomDTO;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.dto.reports.SubmeterDTO;
import com.github.devfrogora.service.exception.BusinessRuleException;
import com.github.devfrogora.service.exception.ResourceNotFoundException;
import com.github.devfrogora.service.exception.RoomOccupiedException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomMeterServiceImpl implements RoomMeterService {

    @Override
    public void addRoomWithMeter(String roomNumber, String roomType, String meterSerialNumber, double meterInitialReading) throws SQLException {
        int roomId;
        Optional<Room> existingRoom = DaoManager.getRoomDao().getRoomByNumber(roomNumber);

        if (existingRoom.isPresent()) {
            roomId = existingRoom.get().getRoomId();
        } else {
            Room room = new Room();
            room.setRoomNumber(roomNumber);
//            room.setRentAmount(roomType.equalsIgnoreCase("2BHK") ? 12000.00 : 7500.00);

            boolean inserted = DaoManager.getRoomDao().insertRoom(room);
            if (!inserted) {
                throw new BusinessRuleException("Failed to provision room asset in the database.");
            }

            roomId = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Room registration failed to verify."))
                    .getRoomId();
        }

        Optional<Submeter> linkedMeters = DaoManager.getSubmeterDao().getSubmeterByRoomId(roomId);
        MeterBillingService  meterBillingService = new MeterBillingServiceImpl();

        if (linkedMeters.isEmpty()) {
            Submeter meter = new Submeter();
            meter.setRoomId(roomId);
            meter.setMeterSerialNumber(meterSerialNumber);
            meter.setInitialReading(meterInitialReading);

            int meterId = DaoManager.getSubmeterDao().insertSubmeter(meter);
            if (meterId < 0) {
                throw new BusinessRuleException("Failed to attach hardware submeter registry.");
            }
            meterBillingService.initialMeterReading(meterId,meterInitialReading,50,10);
        }else {
            meterBillingService.initialMeterReading(linkedMeters.get().getMeterId(), meterInitialReading, 50, 10);
        }

    }

    @Override
    public void deleteRoomIfVacant(String roomNumber) throws SQLException {
        // 1. Check existence
        Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " does not exist."));

        // 2. Structural Constraint Verification
        Optional<Tenancy> activeTenancy = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getRoomId());
        if (activeTenancy.isPresent()) {
            throw new RoomOccupiedException(roomNumber, activeTenancy.get().getTenantId());
        }

        // 3. Cleanup associated assets
        Optional<Submeter> linkedMeter = DaoManager.getSubmeterDao().getSubmeterByRoomId(room.getRoomId());
        if (linkedMeter.isPresent()) {
            DaoManager.getSubmeterDao().deleteSubmeter(linkedMeter.get().getMeterId());
        }

        // 4. Safely drop the room asset records
        boolean isDeleted = DaoManager.getRoomDao().deleteRoom(room.getRoomId());
        if (!isDeleted) {
            throw new BusinessRuleException("Database failure occurred while trying to drop Room: " + roomNumber);
        }
    }

    @Override
    public List<RoomRegistryDto> getRoomRegistryReport() {
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

                registryList.add(new RoomRegistryDto(
                        room.getRoomNumber(),
                        tenantName,
                        serialNumber,
                        isVacant
                ));
            }
        } catch (Exception e) {
            // Log the exception so you don't lose the stack trace
            throw new BusinessRuleException("Error generating room registry report : "+ e);

            // Option A: Return whatever was processed so far (or an empty list if it failed at getAllRooms())
            // Option B: Rethrow a custom runtime exception if the caller MUST know it failed:
            // throw new ReportGenerationException("Failed to fetch room registry", e);
        }

        return registryList;
    }

    @Override
    public SubmeterDTO getSubmeterByRoomNumber(String roomNumber) throws SQLException {
        Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " not found."));
        Optional<Submeter> submeter = DaoManager.getSubmeterDao().getSubmeterByRoomId(room.getRoomId());
        if (submeter.isPresent()) {
            return new SubmeterDTO(submeter.get().getMeterId(), submeter.get().getRoomId(), submeter.get().getMeterSerialNumber(),
                    submeter.get().getInitialReading());
        }
        return null;
    }

    @Override
    public void updateSubmeter(String roomNumber, String oldMeterSerialNumber, String newMeterSerialNumber) throws SQLException {

        Optional<Submeter> submeter = DaoManager.getSubmeterDao().getSubmeterBySerialNumber(oldMeterSerialNumber);
        if (submeter.isPresent()) {
            Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " not found."));
            submeter.get().setRoomId(room.getRoomId());
            submeter.get().setMeterSerialNumber(newMeterSerialNumber);
            DaoManager.getSubmeterDao().updateSubmeter(submeter.get());
        } else {
            throw new ResourceNotFoundException("Meter " + oldMeterSerialNumber + " not found.");
        }
    }


    // Keep query methods clean by returning standard Optional/boolean

    @Override
    public Optional<RoomDTO> findByRoomNumber(String roomNumber) throws SQLException {
        return DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                .map(room -> new RoomDTO(room.getRoomNumber(), room.getRentAmount()));
    }

    @Override
    public boolean isRoomExist(String roomNumber) throws SQLException{
        return DaoManager.getRoomDao().getRoomByNumber(roomNumber).isPresent();
    }

    @Override
    public boolean isRoomVacant(String roomNumber) throws SQLException{
        Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " not found."));

        return DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getRoomId()).isEmpty();
    }
}