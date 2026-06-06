package com.github.devfrogora.service.impl;

import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.Room;
import com.github.devfrogora.data.entities.Tenancy;
import com.github.devfrogora.data.entities.Tenant;
import com.github.devfrogora.data.utils.DateUtils;
import com.github.devfrogora.service.dto.TenancyDTO;
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.exception.*;

import com.github.devfrogora.service.TenancyManagementService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class TenancyManagementServiceImpl implements TenancyManagementService {

    @Override
    public void addTenantWithTenancy(TenantDTO tenantDto, String roomNumber, String startDate) throws SQLException {
        Optional<Tenant> existing = DaoManager.getTenantDao().getTenantByAadhar(tenantDto.getAadharNumber());
        int tenantId;

        if (existing.isEmpty()) {
            Tenant tenantEntity = new Tenant();
            tenantEntity.setName(tenantDto.getName());
            tenantEntity.setAadharNumber(tenantDto.getAadharNumber());
            tenantEntity.setPhoneNumber(tenantDto.getPhoneNumber());

            boolean inserted = DaoManager.getTenantDao().insertTenant(tenantEntity);
            if (!inserted) {
                throw new BusinessRuleException("Failed to register the new tenant profile.");
            }

            tenantId = DaoManager.getTenantDao().getTenantByAadhar(tenantDto.getAadharNumber())
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
    public void vacateRoom(String roomNumber,String endTenancyDate)  throws SQLException{
        Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " does not exist."));

        Optional<Tenancy> activeTenancyOpt = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getRoomId());
        if (activeTenancyOpt.isEmpty()) {
            throw new BusinessRuleException("Room " + roomNumber + " is already vacant. No action taken.");
        }

        Tenancy activeTenancy = activeTenancyOpt.get();
        String endDate = DateUtils.validateDate(endTenancyDate);

        boolean isCheckedOut = DaoManager.getTenancyDao().endContract(
                room.getRoomId(),
                activeTenancy.getTenantId(),
                endDate
        );

        if (!isCheckedOut) {
            throw new BusinessRuleException(" failed to vacate  Room " + roomNumber);
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
    public void terminateTenancyOfRoom(String roomNumber,String endTenancyDate) throws SQLException{
            vacateRoom(roomNumber,endTenancyDate);
    }

    public TenancyDTO findActiveTenancyByRoomNumber(String roomNumber)throws SQLException{
        Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room " + roomNumber + " does not exist."));
        Optional<Tenancy> activeTenancyOpt = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getRoomId());
        if (activeTenancyOpt.isEmpty()) {
//            throw new BusinessRuleException("Room " + roomNumber + " is  vacant ");
            return null;
        }
        Tenancy activeTenancy = activeTenancyOpt.get();
        Optional<Tenant> tenant = DaoManager.getTenantDao().getTenantById(activeTenancy.getTenantId());


        return new TenancyDTO( activeTenancy.getTenancyId(),activeTenancy.getRoomId(),activeTenancy.getTenantId(),roomNumber,
                tenant.get().getAadharNumber(),activeTenancy.getStartDate(),activeTenancy.getEndDate());
    }

    public TenancyDTO findActiveTenancyByTenantAadhar(String aadharNumber)throws SQLException{
        Tenant tenant = DaoManager.getTenantDao().getTenantByAadhar(aadharNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No tenant client matches the given identity key."));
        Optional<Tenancy> activeTenancyOpt = DaoManager.getTenancyDao().getActiveTenancyByRoomId(tenant.getTenantId());

        if (activeTenancyOpt.isEmpty()) {
//            throw new BusinessRuleException("Tenant " + aadharNumber + " is  not exist ");
            return null;
        }
        Tenancy activeTenancy = activeTenancyOpt.get();
        Optional<Room> room = DaoManager.getRoomDao().getRoomById(activeTenancy.getRoomId());

        return new TenancyDTO( activeTenancy.getTenancyId(),activeTenancy.getRoomId(),activeTenancy.getTenantId(),room.get().getRoomNumber(),
                tenant.getAadharNumber(),activeTenancy.getStartDate(),activeTenancy.getEndDate());

    }

    @Override
    public Optional<TenantDTO> findTenantByAadhar(String aadhar) throws SQLException{
        return DaoManager.getTenantDao().getTenantByAadhar(aadhar)
                .map(entity -> new TenantDTO(
                        entity.getName(),
                        entity.getAadharNumber(),
                        entity.getPhoneNumber()
                ));
    }
}