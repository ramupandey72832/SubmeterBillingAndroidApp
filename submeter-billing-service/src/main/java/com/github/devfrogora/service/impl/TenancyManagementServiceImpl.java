package com.github.devfrogora.service.impl;

import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.Room;
import com.github.devfrogora.data.entities.Tenancy;
import com.github.devfrogora.data.entities.Tenant;
import com.github.devfrogora.service.exception.*;

import com.github.devfrogora.service.TenancyManagementService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TenancyManagementServiceImpl implements TenancyManagementService {

    @Override
    public void addTenantWithTenancy(Tenant tenant,String roomNumber, String startDate) throws SQLException {
        Optional<Tenant> existing = DaoManager.getTenantDao().getTenantByAadhar(tenant.getAadharNumber());
        int tenantId;

        if (existing.isEmpty()) {
            boolean inserted = DaoManager.getTenantDao().insertTenant(tenant);
            if (!inserted) {
                throw new BusinessRuleException("Failed to register the new tenant profile.");
            }

            tenantId = DaoManager.getTenantDao().getTenantByAadhar(tenant.getAadharNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant registration failed to verify in database records."))
                    .getTenantId();
        } else {
            tenantId = existing.get().getTenantId();
        }

        // Resolving the latest room allocation
        Optional<Room> room = DaoManager.getRoomDao().getRoomByNumber(roomNumber);
        if (!room.isPresent()) {
            throw new ResourceNotFoundException("Room does not exist!!");
        }
        int activeRoomId = room.get().getRoomId();

        // Check if room is already occupied
        Optional<Tenancy> currentLease = DaoManager.getTenancyDao().getActiveTenancyByRoomId(activeRoomId);
        if (currentLease.isPresent()) {
            throw new BusinessRuleException("Room : "+roomNumber+" is currently occupied by Tenant ID: " + currentLease.get().getTenantId());
        }

        Tenancy lease = new Tenancy();
        lease.setRoomId(activeRoomId);
        lease.setTenantId(tenantId);
        lease.setStartDate(startDate);
        lease.setEndDate(null);

        boolean activeCheckIn = DaoManager.getTenancyDao().insertTenancy(lease);
        if (!activeCheckIn) {
            throw new BusinessRuleException("Database failure occurred while filing the tenancy allocation transaction.");
        }
    }

    @Override
    public void vacateRoom(String roomNumber)  throws SQLException{
        Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " does not exist."));

        Optional<Tenancy> activeTenancyOpt = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getRoomId());
        if (activeTenancyOpt.isEmpty()) {
            throw new BusinessRuleException("Room " + roomNumber + " is already vacant. No action taken.");
        }

        Tenancy activeTenancy = activeTenancyOpt.get();
        String todayIsoString = LocalDate.now().toString();

        boolean isCheckedOut = DaoManager.getTenancyDao().endContract(
                room.getRoomId(),
                activeTenancy.getTenantId(),
                todayIsoString
        );

        if (!isCheckedOut) {
            throw new BusinessRuleException("Database failed to apply checkout timestamp for Room " + roomNumber);
        }
    }

    @Override
    public void deleteTenantIfNoActiveTenancy(String aadharNumber) throws SQLException {
        Tenant tenant = DaoManager.getTenantDao().getTenantByAadhar(aadharNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No tenant client matches the given identity key."));

        // Optimized verification query to see if the client has any open contracts
        boolean hasActiveContracts = DaoManager.getTenancyDao().getAllTenancies().stream()
                .anyMatch(t -> t.getTenantId() == tenant.getTenantId() && t.getEndDate() == null);

        if (hasActiveContracts) {
            throw new BusinessRuleException("Cannot delete tenant profile '" + tenant.getName()
                    + "' because they are currently checked into an active room contract.");
        }

        boolean isDeleted = DaoManager.getTenantDao().deleteTenant(tenant.getTenantId());
        if (!isDeleted) {
            throw new BusinessRuleException("Database write failure occurred while trying to drop tenant profile.");
        }
    }

    /**
     *
     * @param roomNumber
     * @return true if success and false or exception if not
     */
    @Override
    public void terminateTenancyOfRoom(String roomNumber) throws SQLException{

        try {
            // 1. Fetch room details to identify the tenant before vacating
            Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " not found."));

            Optional<Tenancy> activeTenancy = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getRoomId());
            if (activeTenancy.isEmpty()) {
//                System.out.println("Notice: Room " + roomNumber + " is already vacant.");
               throw new ResourceNotFoundException("Tenancy  for " + roomNumber + " not found.");
            }

            int tenantId = activeTenancy.get().getTenantId();
            Tenant tenant = DaoManager.getTenantDao().getTenantById(tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant records missing for internal ID " + tenantId));

            // 2. Perform the checkout (End the contract)
            vacateRoom(roomNumber);
//            System.out.println("Success: Tenancy closed for room " + roomNumber);

            // 3. Delete the profile since they are now completely unattached to any active contracts
            deleteTenantIfNoActiveTenancy(tenant.getAadharNumber());
//            System.out.println("Success: Profile account for " + tenant.getName() + " deleted securely.");

        } catch (BusinessRuleException e) {
            throw new BusinessRuleException("Failed to terminate tenancy");
        }
    }

    @Override
    public Optional<Tenant> findTenantByAadhar(String aadhar) throws SQLException{
        return DaoManager.getTenantDao().getTenantByAadhar(aadhar);
    }
}