package com.github.devfrogora.service;

import com.github.devfrogora.service.dto.BillDTO;
import com.github.devfrogora.service.dto.reports.BillReportDto;

import java.sql.SQLException;
import java.util.List;

public interface MeterBillingService {

    public void initialMeterReading(int submeterId, double initialReading , int fixedCharge, double rate) throws SQLException;
    void addMeterReadingWithGenerateBill(String roomNumber, double currentMeterReading, double rate, double fixedCharge) throws SQLException;
    void updateBillPaymentStatus(int billId, boolean isPaid) throws SQLException;

    List<BillReportDto> getAllBillsReport() throws SQLException;
    public double getLatestReading(String submeterSerialNumber) throws SQLException;
    // Replace Bill entities with BillDTOs
    List<BillDTO> getAllPendingBills() throws SQLException;

    double getTotalUnit() throws SQLException;

    BillDTO getBillById(int billId) throws SQLException;
}