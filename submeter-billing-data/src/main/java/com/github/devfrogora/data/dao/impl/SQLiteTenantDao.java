package com.github.devfrogora.data.dao.impl;

import com.github.devfrogora.data.config.SqlLoader;
import com.github.devfrogora.data.dao.TenantDao;
import com.github.devfrogora.data.dao.DbUtils;
import com.github.devfrogora.data.entities.Tenant;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SQLiteTenantDao implements TenantDao {

    @Override
    public boolean insertTenant(Tenant tenant) throws SQLException{
        String sql = SqlLoader.get("tenant.insert");
        return DbUtils.executeUpdate(sql,
                tenant.getName(),
                tenant.getPhoneNumber(),
                tenant.getAadharNumber(),
                tenant.getAddress(),
                tenant.getEmail(),
                tenant.isActive() ? 1 : 0 // Map boolean flags explicitly to SQLite 0/1 integers
        );
    }

    @Override
    public Optional<Tenant> getTenantById(int tenantId)  throws SQLException{
        String sql = SqlLoader.get("tenant.get_by_id");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToTenant, tenantId);
    }

    @Override
    public List<Tenant> getAllTenants() throws SQLException {
        String sql = SqlLoader.get("tenant.get_all");
        return DbUtils.executeQueryList(sql, this::mapResultSetToTenant);
    }

    @Override
    public List<Tenant> getActiveTenants() throws SQLException {
        String sql = SqlLoader.get("tenant.get_active");
        return DbUtils.executeQueryList(sql, this::mapResultSetToTenant);
    }

    @Override
    public Optional<Tenant> getTenantByAadhar(String aadharNumber) throws SQLException {
        String sql = SqlLoader.get("tenant.get_by_aadhar");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToTenant, aadharNumber);
    }

    @Override
    public boolean updateTenant(Tenant tenant) throws SQLException {
        String sql = SqlLoader.get("tenant.update");
        return DbUtils.executeUpdate(sql,
                tenant.getName(),
                tenant.getPhoneNumber(),
                tenant.getAadharNumber(),
                tenant.getAddress(),
                tenant.getEmail(),
                tenant.isActive() ? 1 : 0,
                tenant.getTenantId()
        );
    }

    @Override
    public boolean deleteTenant(int tenantId) throws SQLException{
        String sql = SqlLoader.get("tenant.delete");
        return DbUtils.executeUpdate(sql, tenantId);
    }

    /**
     * Maps a single row from the ResultSet straight to the Tenant entity object.
     */
    private Tenant mapResultSetToTenant(ResultSet rs) throws SQLException {
        Tenant tenant = new Tenant();
        tenant.setTenantId(rs.getInt("tenant_id"));
        tenant.setName(rs.getString("name"));
        tenant.setPhoneNumber(rs.getString("phone_number"));
        tenant.setAadharNumber(rs.getString("aadhar_number"));
        tenant.setAddress(rs.getString("address"));
        tenant.setEmail(rs.getString("email"));
        tenant.setActive(rs.getBoolean("is_active")); // JDBC automatically resolves 1 to true and 0 to false
        return tenant;
    }
}