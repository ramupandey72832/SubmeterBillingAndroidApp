package com.submeter.data.dao.impl;

import com.submeter.data.dao.TenancyDao;
import com.submeter.data.entities.Tenancy;
import com.example.rrallutilitylib.config.PropertiesLoader;
import com.example.rrallutilitylib.db.JdbcHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SqlLiteTenancyDao implements TenancyDao {

    @Override
    public boolean insertTenancy(Tenancy tenancy) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "tenancy.insert");
        return JdbcHelper.executeUpdate(sql,
                tenancy.getRoomId(),
                tenancy.getTenantId(),
                tenancy.getStartDate(),
                tenancy.getEndDate() // Will accept null for an active check-in
        );
    }

    @Override
    public Optional<Tenancy> getActiveTenancyByRoomId(int roomId) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties","tenancy.get_active_by_room");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToTenancy, roomId);
    }

    @Override
    public List<Tenancy> getTenancyHistoryByRoomId(int roomId) throws SQLException{
        String sql = PropertiesLoader.getProperty("queries.properties","tenancy.get_history_by_room");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToTenancy, roomId);
    }

    @Override
    public boolean endContract(int roomId, int tenantId, String endDate) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties","tenancy.end_contract");
        return JdbcHelper.executeUpdate(sql, endDate, roomId, tenantId);
    }

    @Override
    public List<Tenancy> getAllTenancies()  throws SQLException{
        String sql = PropertiesLoader.getProperty("queries.properties","tenancy.get_all");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToTenancy);
    }

    /**
     * Maps a single row from the ResultSet to a Tenancy entity object.
     */
    private Tenancy mapResultSetToTenancy(ResultSet rs) throws SQLException {
        Tenancy tenancy = new Tenancy();
        tenancy.setId(rs.getInt("tenancy_id"));
        tenancy.setRoomId(rs.getInt("room_id"));
        tenancy.setTenantId(rs.getInt("tenant_id"));
        tenancy.setStartDate(rs.getString("start_date"));

        // Handle potential NULL value safely for the active end date string
        String endDate = rs.getString("end_date");
        tenancy.setEndDate(rs.wasNull() ? null : endDate);

        return tenancy;
    }
}