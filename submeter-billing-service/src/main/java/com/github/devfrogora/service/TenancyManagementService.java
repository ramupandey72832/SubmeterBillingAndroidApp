package com.github.devfrogora.service;

import com.github.devfrogora.data.entities.Tenant;

import java.sql.SQLException;
import java.util.Optional;

public interface TenancyManagementService {
    public void addTenantWithTenancy(Tenant tenant,String roomNumber, String startDate) throws SQLException ;

    Optional<Tenant> findTenantByAadhar(String aadhar) throws SQLException;

    /**
     * Terminates the active tenancy for a given room, marking it vacant.
     * @param roomNumber The physical identifier code of the room (e.g., "302-C").
     * @return true if the room was successfully checked out and marked vacant.
     */
    void vacateRoom(String roomNumber) throws SQLException;
    public void deleteTenantIfNoActiveTenancy(String aadharNumber) throws SQLException;

    void terminateTenancyOfRoom(String roomNumber) throws SQLException;

}