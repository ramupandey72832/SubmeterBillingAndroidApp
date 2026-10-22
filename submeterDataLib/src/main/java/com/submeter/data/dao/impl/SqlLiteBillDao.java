package com.submeter.data.dao.impl;

import com.submeter.data.dao.BillDao;
import com.submeter.data.entities.Bill;
//import com.submeter.data.util.JdbcHelper;
import com.example.rrallutilitylib.config.PropertiesLoader;
import com.example.rrallutilitylib.db.JdbcHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SqlLiteBillDao implements BillDao {

    @Override
    public long insert(Bill bill) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "bill.insert");
        return JdbcHelper.executeInsert(sql,
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
                bill.getBillingDate()
        );
    }

    @Override
    public boolean update(Bill bill) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "bill.update");
        // FIXED: Added missing parameters to line up perfectly with updated query values list
        return JdbcHelper.executeUpdate(sql,
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
                bill.getId()
        );
    }

    @Override
    public boolean deleteById(long billId) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "bill.delete");
        return JdbcHelper.executeUpdate(sql, billId);
    }

    @Override
    public Optional<Bill> findById(long billId) throws SQLException  {
        String sql = PropertiesLoader.getProperty("queries.properties", "bill.get_by_id");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToBill, billId);
    }


    @Override
    public List<Bill> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public List<Bill> findByTenantId(long tenantId) throws SQLException  {
        return List.of();
    }

    @Override
    public List<Bill> findByMeterId(long meterId) throws SQLException  {
        return List.of();
    }

    @Override
    public List<Bill> getBillsByRange(String start, String end) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "bill.get_by_range");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToBill, start, end);
    }

    @Override
    public List<Bill> getLatestThreeMonthBills() throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "bill.get_latest_three_month");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToBill);
    }

    @Override
    public List<Bill> getBillsByStatus(boolean isPaid) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "bill.get_by_status");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToBill, isPaid ? 1 : 0);
    }

    @Override
    public List<Bill> getAllBills() throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "bill.get_all");
        return JdbcHelper.executeQueryList(sql, this::mapResultSetToBill);
    }

    @Override
    public Optional<Bill> getLatestBill(String roomNumber) throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "bill.get_latest_by_room");
        return JdbcHelper.executeQuerySingle(sql, this::mapResultSetToBill, roomNumber);
    }


    public boolean markAsPaid(long billId, String paymentDate)  throws SQLException {
        String sql = PropertiesLoader.getProperty("queries.properties", "bill.update_status");
//        String paymentDate = isPaid ? LocalDate.now().toString() : null;
        return JdbcHelper.executeUpdate(sql, paymentDate , billId);
    }

    /**
     * Helper mapper method to convert a single ResultSet row into a Bill entity instance.
     */
    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setId(rs.getInt("id"));

        // Handling optional/nullable Foreign Keys safely
        int prevReadingId = rs.getInt("previous_reading_id");
        bill.setPreviousReadingId(rs.wasNull() ? null : prevReadingId);

        bill.setCurrentReadingId(rs.getInt("current_reading_id"));
        bill.setMeterId(rs.getInt("meter_id"));
        bill.setMeterSerialNumber(rs.getString("meter_serial_number"));

        int tenantId = rs.getInt("tenant_id");
        bill.setTenantId(rs.wasNull() ? null : tenantId);

        bill.setTenantName(rs.getString("tenant_name"));
        bill.setRoomNumber(rs.getString("room_number"));
        bill.setUnitsConsumed(rs.getLong("units_consumed"));
        bill.setRatePerUnit(rs.getDouble("rate_per_unit"));
        bill.setFixedCharge(rs.getDouble("fixed_charge"));
        bill.setExtraCharge(rs.getDouble("extra_charge"));
        bill.setTotalAmount(rs.getDouble("total_amount"));
        bill.setNote(rs.getString("note"));
        bill.setBillingDate(rs.getString("billing_date"));

        return bill;
    }
}
