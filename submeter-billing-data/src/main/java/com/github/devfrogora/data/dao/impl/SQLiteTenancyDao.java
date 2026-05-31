package com.github.devfrogora.data.dao.impl;

import com.github.devfrogora.data.config.SqlLoader;
import com.github.devfrogora.data.dao.TenancyDao;
import com.github.devfrogora.data.dao.DbUtils;
import com.github.devfrogora.data.entities.Tenancy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteTenancyDao implements TenancyDao {

    @Override
    public boolean insertTenancy(Tenancy tenancy) throws SQLException {
        String sql = SqlLoader.get("tenancy.insert");
        return DbUtils.executeUpdate(sql,
                tenancy.getRoomId(),
                tenancy.getTenantId(),
                tenancy.getStartDate(),
                tenancy.getEndDate() // Will accept null for an active check-in
        );
    }

    @Override
    public Optional<Tenancy> getActiveTenancyByRoomId(int roomId) throws SQLException {
        String sql = SqlLoader.get("tenancy.get_active_by_room");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToTenancy, roomId);
    }

    @Override
    public List<Tenancy> getTenancyHistoryByRoomId(int roomId) throws SQLException{
        String sql = SqlLoader.get("tenancy.get_history_by_room");
        return DbUtils.executeQueryList(sql, this::mapResultSetToTenancy, roomId);
    }

    @Override
    public boolean endContract(int roomId, int tenantId, String endDate) throws SQLException {
        String sql = SqlLoader.get("tenancy.end_contract");
        return DbUtils.executeUpdate(sql, endDate, roomId, tenantId);
    }

    @Override
    public List<Tenancy> getAllTenancies()  throws SQLException{
        String sql = SqlLoader.get("tenancy.get_all");
        return DbUtils.executeQueryList(sql, this::mapResultSetToTenancy);
    }

    /**
     * Maps a single row from the ResultSet to a Tenancy entity object.
     */
    private Tenancy mapResultSetToTenancy(ResultSet rs) throws SQLException {
        Tenancy tenancy = new Tenancy();
        tenancy.setTenancyId(rs.getInt("tenancy_id"));
        tenancy.setRoomId(rs.getInt("room_id"));
        tenancy.setTenantId(rs.getInt("tenant_id"));
        tenancy.setStartDate(rs.getString("start_date"));

        // Handle potential NULL value safely for the active end date string
        String endDate = rs.getString("end_date");
        tenancy.setEndDate(rs.wasNull() ? null : endDate);

        return tenancy;
    }
}