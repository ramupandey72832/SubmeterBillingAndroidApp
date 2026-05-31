package com.github.devfrogora.service;

import com.github.devfrogora.data.entities.Bill;
import com.github.devfrogora.service.dto.BillReportDto;

import java.sql.SQLException;
import java.util.List;

public interface MeterBillingService {

    void addMeterReadingWithGenerateBill(String roomNumber, double currentMeterReading, double rate, double fixedCharge) throws SQLException;
    void updateBillPaymentStatus(int billId, boolean isPaid) throws SQLException;

    List<BillReportDto> getAllBillsReport() throws SQLException;

    List<Bill> getAllPendingBills() throws SQLException;

    double getTotalUnit() throws SQLException;
}