package com.submeter.data.dao.impl;

import com.submeter.data.dao.TenantDao;
import com.submeter.data.entities.Tenant;
import com.example.rrallutilitylib.config.PropertiesLoader;
import com.example.rrallutilitylib.db.JdbcHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SqlLiteTenantDao implements TenantDao {
    @Override
    public boolean insertTenant(Tenant tenant) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "tenant.insert");
        return JdbcHelper.executeUpdate(sql,
                tenant.getName(),
                tenant.getPhoneNumber(),
                tenant.getAadhaarNumber(),
                tenant.getAddress()
        );
    }

    @Override
    public Optional<Tenant> getTenantById(int tenantId)  throws SQLException{
        String sql = PropertiesLoader.getProperty("queries.properties", "tenant.get_by_id");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToTenant, tenantId);
    }

    @Override
    public List<Tenant> getAllTenants() throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "tenant.get_all");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToTenant);
    }

    @Override
    public List<Tenant> getActiveTenants() throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "tenant.get_active");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToTenant);
    }

    @Override
    public Optional<Tenant> getTenantByAadhar(String aadharNumber) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "tenant.get_by_aadhar");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToTenant, aadharNumber);
    }

    @Override
    public boolean updateTenant(Tenant tenant) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "tenant.update");
        return JdbcHelper.executeUpdate(sql,
                tenant.getName(),
                tenant.getPhoneNumber(),
                tenant.getAadhaarNumber(),
                tenant.getAddress(),
                tenant.getId()
        );
    }

    @Override
    public boolean deleteTenant(int tenantId) throws SQLException{
        String sql = PropertiesLoader.getProperty("queries.properties", "tenant.delete");
        return JdbcHelper.executeUpdate(sql, tenantId);
    }

    /**
     * Maps a single row from the ResultSet straight to the Tenant entity object.
     */
    private Tenant mapResultSetToTenant(ResultSet rs) throws SQLException {
        Tenant tenant = new Tenant();
        tenant.setId(rs.getInt("tenant_id"));
        tenant.setName(rs.getString("name"));
        tenant.setPhoneNumber(rs.getString("phone_number"));
        tenant.setAadhaarNumber(rs.getString("aadhar_number"));
        tenant.setAddress(rs.getString("address"));
        return tenant;
    }
}