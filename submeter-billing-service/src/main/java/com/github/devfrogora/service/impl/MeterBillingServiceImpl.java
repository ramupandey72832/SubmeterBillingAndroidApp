package com.github.devfrogora.service.impl;

import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.*;
import com.github.devfrogora.data.utils.DateUtils;
import com.github.devfrogora.service.dto.BillDTO;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.dto.reports.SubmeterDTO;
import com.github.devfrogora.service.exception.*;
import com.github.devfrogora.service.MeterBillingService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MeterBillingServiceImpl implements MeterBillingService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Checks if a reading already exists for the given month/year.
     * Throws an exception instead of returning a misleading boolean flag.
     */
    private void validateMeterReadingDate(int meterId, String readingDate) throws SQLException {
        LocalDate incomingDate = LocalDate.parse(readingDate, DATE_FORMATTER);
        int incomingYear = incomingDate.getYear();
        int incomingMonth = incomingDate.getMonthValue();

        List<MeterReading> historicalReadings = DaoManager.getMeterReadingDao().getReadingsByMeterId(meterId);

        boolean monthAlreadyRecorded = historicalReadings.stream().anyMatch(reading -> {
            LocalDate existingDate = LocalDate.parse(reading.getReadingDate(), DATE_FORMATTER);
            return existingDate.getYear() == incomingYear && existingDate.getMonthValue() == incomingMonth;
        });

        if (monthAlreadyRecorded) {
            throw new BusinessRuleException("A meter reading has already been logged for this calendar month ("
                    + incomingDate.getMonth() + " " + incomingYear + ").");
        }
    }

    @Override
    public void initialMeterReading(int submeterId, double initialReading , int fixedCharge, double rate) throws SQLException {
        Optional<Submeter> submeter = DaoManager.getSubmeterDao().getSubmeterById(submeterId);

        if(submeter.isEmpty()){
            throw new SQLException("No hardware meter profiles bound to unit number: " + submeterId);
        }
        String todayIsoString = LocalDate.now().toString();
        todayIsoString = DateUtils.validateDate(todayIsoString);
        MeterReading currentReading = new MeterReading();
        currentReading.setMeterId(submeter.get().getMeterId());
        currentReading.setReadingValue(initialReading);
        currentReading.setImageUrlOrPath(null);
        currentReading.setReadingDate(todayIsoString);

        int insertedReadingId = DaoManager.getMeterReadingDao().insertReading(currentReading);
        Room room = DaoManager.getRoomDao().getRoomById(submeter.get().getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room ID " + submeter.get().getRoomId() + " not found."));
        String roomNumber = room.getRoomNumber();

        addIntialMeterReading(submeter.get().getRoomId(),roomNumber,insertedReadingId, initialReading, fixedCharge , rate);
    }

    void addIntialMeterReading(int roomId,String roomNumber,int currentReadingId ,
                               double currentMeterReading , int fixedCharge, double rate  ) throws SQLException{
        // 5. Query active lease profile for tenant details
        Optional<Tenancy> activeLease = DaoManager.getTenancyDao().getActiveTenancyByRoomId(roomId);
        String tenantName = "Vacant Unit Asset";
        if (activeLease.isPresent()) {
            Tenant currentOccupant = DaoManager.getTenantDao().getTenantById(activeLease.get().getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active tenancy is bound to a non-existent Tenant profile ID."));
            tenantName = currentOccupant.getName();
        }

        // 6. Pass values directly down to the billing engine
        int billId = generateBill(
                tenantName,
                roomNumber,
                currentReadingId,
                currentMeterReading,
                0,0,
                fixedCharge,
                rate
        );

        updateBillPaymentStatus(billId,true);
    }

    @Override
    public void addMeterReadingWithGenerateBill(String roomNumber, double currentMeterReading, double rate, double fixedCharge)throws SQLException  {
        // 1. Resolve room and submeter
        Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Target unit identification code not found: " + roomNumber));

        Submeter submeter = DaoManager.getSubmeterDao().getSubmeterByRoomId(room.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("No hardware meter profiles bound to unit number: " + roomNumber));

        // 2. Resolve historical readings and baseline properties
        Optional<MeterReading> latestDbReading = DaoManager.getMeterReadingDao().getLatestReadingByMeterId(submeter.getMeterId());

        int previousReadingId = latestDbReading.map(MeterReading::getReadingId).orElse(0);
        double previousValue = latestDbReading.map(MeterReading::getReadingValue).orElse(submeter.getInitialReading());

        // Business Validation: Prevent negative utility consumption processing
        if (currentMeterReading < previousValue) {
            throw new BusinessRuleException("Input reading (" + currentMeterReading
                    + ") cannot be lower than the previous recorded baseline (" + previousValue + ").");
        }

        // 3. Verify calendar constraints
        String todayIsoString = LocalDate.now().toString();
        validateMeterReadingDate(submeter.getMeterId(), todayIsoString);

        // 4. Record current tracking entries
        MeterReading currentReading = new MeterReading();
        currentReading.setMeterId(submeter.getMeterId());
        currentReading.setReadingValue(currentMeterReading);
        currentReading.setImageUrlOrPath(null);
        currentReading.setReadingDate(todayIsoString);

        boolean isReadingSaved = DaoManager.getMeterReadingDao().insertReading(currentReading) > 0;
        if (!isReadingSaved) {
            throw new BusinessRuleException("Failed to persist current utility meter reading in the ledger database.");
        }

        int currentReadingId = DaoManager.getMeterReadingDao().getLatestReadingByMeterId(submeter.getMeterId())
                .orElseThrow(() -> new BusinessRuleException("Database verification routine failed to fetch active reading ID."))
                .getReadingId();

        // 5. Query active lease profile for tenant details
        Optional<Tenancy> activeLease = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getRoomId());
        String tenantName = "Vacant Unit Asset";
        if (activeLease.isPresent()) {
            Tenant currentOccupant = DaoManager.getTenantDao().getTenantById(activeLease.get().getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active tenancy is bound to a non-existent Tenant profile ID."));
            tenantName = currentOccupant.getName();
        }

        // 6. Pass values directly down to the billing engine
        generateBill(
                tenantName,
                roomNumber,
                currentReadingId,
                currentMeterReading,
                previousReadingId,
                previousValue,
                fixedCharge,
                rate
        );
    }

    /**
     * Core Invoice Processing Component.
     */
    private int generateBill(String tenantName, String roomNumber, int currentReadingId, double currentReading,
                              int previousReadingId, double previousReading, double fixedCharge, double ratePerUnit) throws SQLException {

        double consumption = currentReading - previousReading;
        double usageCost = consumption * ratePerUnit;
        double totalDue = usageCost + fixedCharge;

        Bill bill = new Bill();
        bill.setPreviousReadingId(previousReadingId);
        bill.setCurrentReadingId(currentReadingId);
        bill.setTenantName(tenantName);
        bill.setMeterSerialNumber("serialNumber");
        bill.setMeterId(0);
        bill.setTenantId(null);
        bill.setPreviousReadingId(null);
        bill.setUnitsConsumed(consumption);
        bill.setRatePerUnit(ratePerUnit);
        bill.setTotalAmount(totalDue);
        bill.setBillingDate(LocalDate.now().toString());
        bill.setPaid(false);
        int billId = DaoManager.getBillDao().insertBill(bill);
        if ( billId < 0 ) {
            throw new BusinessRuleException("Database transactional error: Failed to generate invoice statement ledger for unit " + roomNumber);
        }
        return billId;
    }

    @Override
    public void updateBillPaymentStatus(int billId, boolean isPaid) throws SQLException{
        Bill bill = DaoManager.getBillDao().getBillById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot update payment status. Bill ID " + billId + " not found."));

        // Optimization Check: Skip database write if the status matches the intent
        if (bill.isPaid() == isPaid) {
            return;
        }

        boolean isUpdated = DaoManager.getBillDao().updatePaymentStatus(billId, isPaid);
        if (!isUpdated) {
            throw new BusinessRuleException("Failed to update database payment tracking record for Bill ID: " + billId);
        }
    }

    @Override
    public List<BillReportDto> getAllBillsReport() throws SQLException {
        List<Bill> bills = DaoManager.getBillDao().getAllBills();
        List<BillReportDto> reportList = new ArrayList<>();

        for (Bill bill : bills) {
            String roomNumber = "Unknown";
            String tenantName = "Vacant Asset";

            // Stitch database relations safely in the business layer
            Optional<MeterReading> reading = DaoManager.getMeterReadingDao().getReadingById(bill.getCurrentReadingId());
            if (reading.isPresent()) {
                Optional<Submeter> submeter = DaoManager.getSubmeterDao().getSubmeterById(reading.get().getMeterId());
                if (submeter.isPresent()) {
                    Optional<Room> room = DaoManager.getRoomDao().getRoomById(submeter.get().getRoomId());
                    if (room.isPresent()) {
                        roomNumber = room.get().getRoomNumber();

                        Optional<Tenancy> tenancy = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.get().getRoomId());
                        if (tenancy.isPresent()) {
                            tenantName = DaoManager.getTenantDao().getTenantById(tenancy.get().getTenantId())
                                    .map(Tenant::getName).orElse("Vacant Asset");
                        }
                    }
                }
            }

            reportList.add(new BillReportDto(
                    bill.getBillId(),
                    roomNumber,
                    tenantName,
                    bill.getBillingDate(),
                    bill.getTotalAmount(),
                    bill.isPaid() ? "PAID" : "UNPAID"
            ));
        }
        return reportList;
    }

    @Override
    public List<BillDTO> getAllPendingBills() throws SQLException {
        List<Bill> bills = DaoManager.getBillDao().getAllBills();

        long pendingCount = bills.stream().filter(b -> !b.isPaid()).count();
        if (pendingCount == 0) {
            return null;
        }

        // Change .toList() to .collect(Collectors.toList())
        return bills.stream()
                .filter(b -> !b.isPaid())
                .map(b -> new BillDTO(
                        b.getBillId(),
                        b.getBillingDate(),
                        b.getUnitsConsumed(),
                        b.getRatePerUnit(),
                        b.getTotalAmount(),
                        b.isPaid()
                ))
                .collect(Collectors.toList());
    }

    public double getLatestReading(String submeterSerialNumber) throws SQLException{
        Optional<Submeter> submeter = DaoManager.getSubmeterDao().getSubmeterBySerialNumber(submeterSerialNumber);
        Optional<MeterReading> latestReading = DaoManager.getMeterReadingDao().getLatestReadingByMeterId(submeter.get().getMeterId());

        if(latestReading.isEmpty()){
            return submeter.get().getInitialReading();
        }
        return latestReading.get().getReadingValue();
    }

    @Override
    public double getTotalUnit() throws SQLException {
        List<Bill> bills = DaoManager.getBillDao().getAllBills();
        double totalUnits = bills.stream()
                .mapToDouble(Bill::getUnitsConsumed)
                .sum();
        return totalUnits;
    }
}