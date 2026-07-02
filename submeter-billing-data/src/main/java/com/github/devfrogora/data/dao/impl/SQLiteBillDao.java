package com.github.devfrogora.data.dao.impl;

import com.github.devfrogora.data.config.SqlLoader;
import com.github.devfrogora.data.dao.BillDao;
import com.github.devfrogora.data.dao.DbUtils;
import com.github.devfrogora.data.entities.Bill;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SQLiteBillDao implements BillDao {

    @Override
    public int insertBill(Bill bill) throws SQLException {
        return DbUtils.executeInsert(SqlLoader.get("bill.insert"),
                bill.getPreviousReadingId(), // Can be null
                bill.getCurrentReadingId(),
                bill.getMeterId(),
                bill.getMeterSerialNumber(),
                bill.getTenantId(),          // Can be null
                bill.getTenantName(),        // Can be null
                bill.getRoomNumber(),
                bill.getUnitsConsumed(),
                bill.getRatePerUnit(),
                bill.getFixedCharge(),
                bill.getExtraCharge(),
                bill.getTotalAmount(),
                bill.getNote(),
                bill.getBillingDate(),
                bill.getPaymentDate(),
                bill.isPaid() ? 1 : 0
        );
    }

    @Override
    public Optional<Bill> getBillById(int billId) throws SQLException {
        String sql = SqlLoader.get("bill.get_by_id");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToBill, billId);
    }

    public List<Bill> getBillsByRoomNumber(String roomNumber) throws SQLException {
        String sql = SqlLoader.get("bill.get_by_room");
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, roomNumber);
    }

    @Override
    public List<Bill> getBillsByRoomId(int roomId) throws SQLException {
        // Updated to safely match fields returned in mapResultSetToBill
        String sql = "SELECT b.*, r.room_number FROM bills b " +
                "JOIN meter_readings mc ON b.current_reading_id = mc.reading_id " +
                "JOIN submeters sm ON mc.meter_id = sm.meter_id " +
                "JOIN rooms r ON sm.room_id = r.room_id " +
                "WHERE sm.room_id = ?;";
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, roomId);
    }

    public List<Bill> getBillsByTenantName(String tenantName) throws SQLException {
        String sql = SqlLoader.get("bill.get_by_tenant");
        // Expects two parameters now based on the query update: name pattern OR exact structural match
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, "%" + tenantName + "%", tenantName);
    }

    @Override
    public List<Bill> getBillsByTenantId(int tenantId) throws SQLException {
        // Streamlined to pull instantly from the newly added column structure
        String sql = "SELECT b.*, r.room_number FROM bills b " +
                "JOIN meter_readings mc ON b.current_reading_id = mc.reading_id " +
                "JOIN submeters sm ON mc.meter_id = sm.meter_id " +
                "JOIN rooms r ON sm.room_id = r.room_id " +
                "WHERE b.tenant_id = ?;";
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, tenantId);
    }

    @Override
    public List<Bill> getBillsByStatus(boolean isPaid) throws SQLException {
        String sql = SqlLoader.get("bill.get_by_status");
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, isPaid ? 1 : 0);
    }

    @Override
    public List<Bill> getAllBills() throws SQLException {
        String sql = SqlLoader.get("bill.get_all");
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill);
    }


    @Override
    public List<Bill> getBillsByRange(String start, String end) throws SQLException {
        String sql = SqlLoader.get("bill.get_by_range");
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill, start, end);
    }

    @Override
    public List<Bill> getLatestThreeMonthBills() throws SQLException {
        String sql = SqlLoader.get("bill.get_latest_three_month");
        return DbUtils.executeQueryList(sql, this::mapResultSetToBill);
    }


    @Override
    public boolean updateBill(Bill bill) throws SQLException {
        String sql = SqlLoader.get("bill.update");
        // FIXED: Added missing parameters to line up perfectly with updated query values list
        return DbUtils.executeUpdate(sql,
                bill.getPreviousReadingId(),
                bill.getCurrentReadingId(),
                bill.getMeterId(),
                bill.getMeterSerialNumber(),
                bill.getTenantId(),
                bill.getTenantName(),
                bill.getRoomNumber(),
                bill.getUnitsConsumed(),
                bill.getRatePerUnit(),
                bill.getFixedCharge(),
                bill.getExtraCharge(),
                bill.getTotalAmount(),
                bill.getNote(),
                bill.getBillingDate(),
                bill.getPaymentDate(),
                bill.isPaid() ? 1 : 0,
                bill.getBillId()
        );
    }

    @Override
    public boolean updatePaymentStatus(int billId, boolean isPaid) throws SQLException {
        String sql = SqlLoader.get("bill.update_status");
        String todayIsoString = new SimpleDateFormat("yyyy-MM-dd").format(LocalDate.now());

        return DbUtils.executeUpdate(sql, todayIsoString ,isPaid ? 1 : 0, billId);
    }

    @Override
    public boolean deleteBill(int billId) throws SQLException {
        String sql = SqlLoader.get("bill.delete");
        return DbUtils.executeUpdate(sql, billId);
    }

    @Override
    public Optional<Bill> getLatestBill(String roomNumber) throws SQLException {
        String sql = SqlLoader.get("bill.get_latest_by_room");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToBill, roomNumber);
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));

        // FIXED: Native safely handled null values assignment checking using rs.getObject()
        Integer prevId = rs.getObject("previous_reading_id") != null ? rs.getInt("previous_reading_id") : null;
        bill.setPreviousReadingId(prevId);

        bill.setCurrentReadingId(rs.getInt("current_reading_id"));
        bill.setMeterId(rs.getInt("meter_id"));

        Integer tenantId = rs.getObject("tenant_id") != null ? rs.getInt("tenant_id") : null;
        bill.setTenantId(tenantId);

        bill.setTenantName(rs.getString("tenant_name"));
        bill.setMeterSerialNumber(rs.getString("meter_serial_number"));
        bill.setUnitsConsumed(rs.getDouble("units_consumed"));
        bill.setRatePerUnit(rs.getDouble("rate_per_unit"));
        bill.setRoomNumber(rs.getString("room_number"));
        bill.setFixedCharge(rs.getDouble("fixed_charge"));
        bill.setTotalAmount(rs.getDouble("total_amount"));
        bill.setBillingDate(rs.getString("billing_date"));
        bill.setPaid(rs.getBoolean("paid"));

        bill.setPaymentDate(rs.getString("payment_date"));
        bill.setExtraCharge(rs.getDouble("extra_charge"));
        bill.setNote(rs.getString("note"));
        return bill;
    }
}