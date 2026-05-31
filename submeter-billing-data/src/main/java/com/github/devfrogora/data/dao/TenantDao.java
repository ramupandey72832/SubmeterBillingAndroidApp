package com.github.devfrogora.data.dao;

import com.github.devfrogora.data.entities.Tenant;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TenantDao {
    // Create
    boolean insertTenant(Tenant tenant) throws SQLException;

    // Read
    Optional<Tenant> getTenantById(int tenantId) throws SQLException;
    Optional<Tenant> getTenantByAadhar(String aadharNumber) throws SQLException;
    List<Tenant> getAllTenants() throws SQLException;
    List<Tenant> getActiveTenants() throws SQLException;

    // Update
    boolean updateTenant(Tenant tenant) throws SQLException;

    // Delete
    boolean deleteTenant(int tenantId) throws SQLException;
}