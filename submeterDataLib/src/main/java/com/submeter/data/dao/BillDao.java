package com.submeter.data.dao;

import com.submeter.data.entities.Bill;
import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface BillDao {
    long insert(Bill bill) throws SQLException;
    boolean update(Bill bill) throws SQLException;
    boolean deleteById(long billId) throws SQLException;
    Optional<Bill> findById(long billId) throws SQLException;
    List<Bill> findAll() throws SQLException;
    List<Bill> findByTenantId(long tenantId) throws SQLException;
    List<Bill> findByMeterId(long meterId) throws SQLException;
//    boolean markAsPaid(long billId, String paymentDate) throws SQLException;
    List<Bill> getBillsByStatus(boolean isPaid) throws SQLException;
    List<Bill> getAllBills() throws SQLException;
    Optional<Bill> getLatestBill(String roomNumber) throws SQLException;
    List<Bill> getLatestThreeMonthBills() throws SQLException;
    List<Bill> getBillsByRange(String start, String end) throws SQLException;
}