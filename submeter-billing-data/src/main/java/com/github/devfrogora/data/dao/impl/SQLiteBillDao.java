package com.github.devfrogora.data.dao.impl;

import com.github.devfrogora.data.config.SqlLoader;
import com.github.devfrogora.data.dao.BillDao;
import com.github.devfrogora.data.dao.DbUtils;
import com.github.devfrogora.data.entities.Bill;

import org.jdbi.v3.core.Sql;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class SQLiteBillDao implements BillDao {

    @Override
    public int insertBill(Bill bill) throws SQLException {
        return DbUtils.executeInsert(SqlLoader.get("bill.insert"),
                bill.getPreviousReadingId(),
                bill.getCurrentReadingId(),
                bill.getMeterId(),
                bill.getMeterSerialNumber(),
                bill.getTenantId(),
                bill.getTenantName(),
                bill.getUnitsConsumed(),
                bill.getRatePerUnit(),
                bill.getTotalAmount(),
                bill.getBillingDate(),
                bill.isPaid() ? 1 : 0 // Explicitly map boolean flag to integer status
        );
    }

    @Override
    public Optional<Bill> getBillById(int billId) throws SQLException {
        String sql = SqlLoader.get("bill.get_by_id");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToBill, billId);
    }

    // FIXED: Adjusted to pass roomNumber string to match the properties query setup
    public List<Bill> getBillsByRoomNumber(String roomNumber) throws SQLException{
        String sql = SqlLoader.get("bill.get_by_room");
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, roomNumber);
    }

    // ALTERNATIVE: If your interface strictly dictates using the ID instead,
    // change the query in your properties file to: WHERE r.room_id = ?
    @Override
    public List<Bill> getBillsByRoomId(int roomId) throws SQLException {
        String sql = "SELECT b.* FROM bills b " +
                "JOIN meter_readings mc ON b.current_reading_id = mc.reading_id " +
                "JOIN submeters sm ON mc.meter_id = sm.meter_id " +
                "WHERE sm.room_id = ?;";
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, roomId);
    }

    // FIXED: Adjusted to pull via String match.
    // If you need it by ID, change the WHERE clause in properties to: WHERE ten.tenant_id = ?
    public List<Bill> getBillsByTenantName(String tenantName)  throws SQLException{
        String sql = SqlLoader.get("bill.get_by_tenant");
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, "%" + tenantName + "%");
    }

    @Override
    public List<Bill> getBillsByTenantId(int tenantId) throws SQLException{
        String sql = "SELECT b.* FROM bills b " +
                "JOIN meter_readings mc ON b.current_reading_id = mc.reading_id " +
                "JOIN submeters sm ON mc.meter_id = sm.meter_id " +
                "JOIN rooms r ON sm.room_id = r.room_id " +
                "LEFT JOIN tenancies ten ON r.room_id = ten.room_id AND (b.billing_date BETWEEN ten.start_date AND COALESCE(ten.end_date, '9999-12-31')) " +
                "WHERE ten.tenant_id = ?;";
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, tenantId);
    }

    // FIXED: Replaced hardcoded List.of() placeholder with proper DB execution path
    @Override
    public List<Bill> getBillsByStatus(boolean isPaid) throws SQLException{
        String sql = SqlLoader.get("bill.get_by_status");
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, isPaid ? 1 : 0);
    }

    @Override
    public List<Bill> getAllBills() throws SQLException{
        String sql = SqlLoader.get("bill.get_all");
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill);
    }

    @Override
    public boolean updateBill(Bill bill)throws SQLException {
        String sql = SqlLoader.get("bill.update");
        return DbUtils.executeUpdate(sql,
                bill.getPreviousReadingId(),
                bill.getCurrentReadingId(),
                bill.getUnitsConsumed(),
                bill.getRatePerUnit(),
                bill.getTotalAmount(),
                bill.getBillingDate(),
                bill.isPaid() ? 1 : 0,
                bill.getBillId()
        );
    }

    // FIXED: Implemented functional update query string block execution logic
    @Override
    public boolean updatePaymentStatus(int billId, boolean isPaid) throws SQLException {
        String sql = SqlLoader.get("bill.update_status");
        return DbUtils.executeUpdate(sql, isPaid ? 1 : 0, billId);
    }

    @Override
    public boolean deleteBill(int billId) throws SQLException{
        String sql = SqlLoader.get("bill.delete");
        return DbUtils.executeUpdate(sql, billId);
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));
        bill.setPreviousReadingId(rs.getInt("previous_reading_id"));
        bill.setCurrentReadingId(rs.getInt("current_reading_id"));
        bill.setUnitsConsumed(rs.getDouble("units_consumed"));
        bill.setRatePerUnit(rs.getDouble("rate_per_unit"));
        bill.setTotalAmount(rs.getDouble("total_amount"));
        bill.setBillingDate(rs.getString("billing_date"));
        bill.setPaid(rs.getBoolean("paid"));
        return bill;
    }
}