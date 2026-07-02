package com.github.devfrogora.service;

import com.github.devfrogora.service.dto.BillDTO;
import com.github.devfrogora.service.dto.reports.BillReportDto;

import java.sql.SQLException;
import java.util.List;

public interface MeterBillingService {

    public void initialMeterReading(int submeterId, double initialReading , int fixedCharge, double rate) throws SQLException;
    void addMeterReadingAndGenerateBill(String roomNumber, double currentMeterReading, double rate, double fixedCharge,double extraCharge, String notes) throws SQLException;
    void updateBillPaymentStatus(int billId, boolean isPaid) throws SQLException;

    BillReportDto getLatestBill(String roomNumber) throws SQLException;
    List<BillReportDto> getAllBillsReport() throws SQLException;
    public double getLatestReading(String submeterSerialNumber) throws SQLException;
    public List<BillReportDto> getLatestThreeMonthBills() throws SQLException;
    // Replace Bill entities with BillDTOs
    List<BillReportDto> getAllPendingBills() throws SQLException;

    double getTotalUnit() throws SQLException;

    List<BillReportDto> getBillsByRange(String start,String end) throws SQLException;

    BillDTO getBillById(int billId) throws SQLException;

    void checkAndRunMigrations(MigrationCallback callback);// New interface for decoupling messages
    interface MigrationCallback {
        void onMessage(String msg);
        void onError(String err, Exception e);
    }
}