package com.github.devfrogora.data.dao.impl;

import com.github.devfrogora.data.config.SqlLoader;
import com.github.devfrogora.data.dao.BillSummaryDao;
import com.github.devfrogora.data.dao.DbUtils;
import com.github.devfrogora.data.entities.BillSummary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SQLiteBillSummaryDao implements BillSummaryDao  {

    @Override
    public Optional<BillSummary> getSummaryById(int billId)  throws SQLException {
        String sql = SqlLoader.get("billView.get_by_id");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToSummary, billId);
    }

    @Override
    public List<BillSummary> getSummariesByRoom(String roomNumber) throws SQLException {
        String sql = SqlLoader.get("billView.get_by_room");
        return DbUtils.executeQueryList(sql, this::mapResultSetToSummary, roomNumber);
    }

    @Override
    public List<BillSummary> getSummariesByTenantName(String tenantName) throws SQLException {
        String sql = SqlLoader.get("billView.get_by_tenant");
        // Wraps parameter with wildcards safely for SQL LIKE string matching
        return DbUtils.executeQueryList(sql, this::mapResultSetToSummary, "%" + tenantName + "%");
    }

    @Override
    public List<BillSummary> getSummariesByStatus(boolean isPaid) throws SQLException {
        String sql = SqlLoader.get("billView.get_by_status");
        return DbUtils.executeQueryList(sql, this::mapResultSetToSummary, isPaid ? 1 : 0);
    }

    @Override
    public List<BillSummary> getAllSummaries() throws SQLException {
        String sql = SqlLoader.get("billView.get_all");
        return DbUtils.executeQueryList(sql, this::mapResultSetToSummary);
    }

    /**
     * Maps a single row from the database View straight to the Java model entity.
     */
    private BillSummary mapResultSetToSummary(ResultSet rs) throws SQLException {
        BillSummary summary = new BillSummary();
        summary.setBillId(rs.getInt("bill_id"));
        summary.setRoomNumber(rs.getString("room_number"));
        summary.setTenantName(rs.getString("tenant_name"));
        summary.setMeterSerialNumber(rs.getString("meter_serial_number"));
        summary.setPreviousReading(rs.getDouble("previous_reading"));
        summary.setCurrentReading(rs.getDouble("current_reading"));
        summary.setUnitsConsumed(rs.getDouble("units_consumed"));
        summary.setRatePerUnit(rs.getDouble("rate_per_unit"));
        summary.setTotalAmount(rs.getDouble("total_amount"));
        summary.setBillingDate(rs.getString("billing_date"));
        summary.setPaid(rs.getBoolean("paid"));
        return summary;
    }
}