package com.github.devfrogora.data.dao;

import com.github.devfrogora.data.entities.Bill;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BillDao {
    // Create
    int insertBill(Bill bill) throws SQLException;

    // Read
    Optional<Bill> getBillById(int billId) throws SQLException;
    List<Bill> getBillsByRoomId(int roomId) throws SQLException;
    List<Bill> getBillsByTenantId(int tenantId) throws SQLException;

    // Updated to use boolean matching your Bill entity definition
    List<Bill> getBillsByStatus(boolean isPaid) throws SQLException;
    List<Bill> getAllBills() throws SQLException;

    public List<Bill> getBillsByRange(String start, String end) throws SQLException;

    // Update
    boolean updateBill(Bill bill)throws SQLException;

    // Updated to use boolean for cleaner state updates
    boolean updatePaymentStatus(int billId, boolean isPaid) throws SQLException;

    // Delete
    boolean deleteBill(int billId)throws SQLException;

    Optional<Bill> getLatestBill(String roomNumber) throws SQLException;
}