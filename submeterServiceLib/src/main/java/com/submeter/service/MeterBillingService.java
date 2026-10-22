package com.submeter.service;

import com.submeter.commonlib.dto.BillDTO;

import java.sql.SQLException;
import java.util.List;

public interface MeterBillingService {

    public void initialMeterReading(int submeterId, long initialReading , double fixedCharge, double rate) throws SQLException;
    void addMeterReadingAndGenerateBill(String roomNumber, long currentMeterReading, double rate, double fixedCharge,double extraCharge, String notes) throws SQLException;
    void updateBillPaymentStatus(long billId, String paymentDate) throws SQLException;

    BillDTO getLatestBill(String roomNumber) throws SQLException;
    List<BillDTO> getAllBillsReport() throws SQLException;
    public double getLatestReading(String submeterSerialNumber) throws SQLException;
    public List<BillDTO> getLatestThreeMonthBills() throws SQLException;
    // Replace Bill entities with BillDTOs
    List<BillDTO> getAllPendingBills() throws SQLException;

    double getTotalUnit() throws SQLException;

    List<BillDTO> getBillsByRange(String start,String end) throws SQLException;

    BillDTO getBillById(int billId) throws SQLException;
}