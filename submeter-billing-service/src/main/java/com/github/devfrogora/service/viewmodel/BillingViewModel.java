// File: submeter-billing-service/.../service/viewmodel/BillingViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.data.config.DatabaseConnection;
import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.dao.DbUtils;
import com.github.devfrogora.data.entities.Bill;
import com.github.devfrogora.data.entities.MeterReading;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.dto.reports.BillReportDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BillingViewModel {

    private final MeterBillingService meterBillingService;

    // Primitive UI layout states
    private boolean isLoading = false;
    private String errorMessage = null;
    private boolean isOperationSuccess = false;

    // In-Memory data arrays caching states safely across configuration shifts
    private List<BillReportDto> fullBillList = new ArrayList<>();
    private List<BillReportDto> filteredBillList = new ArrayList<>();

    public interface StateListener {
        void onStateChanged();
    }

    private StateListener listener;

    public BillingViewModel(MeterBillingService meterBillingService) {
        this.meterBillingService = meterBillingService;
    }

    public void setStateListener(StateListener listener) {
        this.listener = listener;
    }

    private void notifyUi() {
        if (listener != null) {
            listener.onStateChanged();
        }
    }

    // --- State Getters for the View Layer ---
    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isOperationSuccess() { return isOperationSuccess; }
    public List<BillReportDto> getFilteredBillList() { return filteredBillList; }

    // --- UI Actions ---

    public void fetchBillsReport() {
        this.isLoading = true;
        this.errorMessage = null;
        this.isOperationSuccess = false;
        notifyUi();

        new Thread(() -> {
            try {
                List<BillReportDto> bills = meterBillingService.getAllBillsReport();

                if (bills != null) {
                    this.fullBillList = new ArrayList<>(bills);
                    // Sort descending by Bill ID natively inside the Business Tier
                    Collections.sort(this.fullBillList, Comparator.comparing(BillReportDto::getBillId).reversed());
                } else {
                    this.fullBillList = new ArrayList<>();
                }

                // Default initial filtered list state equals full data cache
                this.filteredBillList = new ArrayList<>(this.fullBillList);
                this.isOperationSuccess = true;

            } catch (Exception e) {
                this.errorMessage = "Database Error while compilation of financial reports: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    /**
     * Executes localized search text parsing metrics in memory instantly 
     * without hitting SQLite resources again.
     */
    public void filterBills(String text) {
        if (text == null || text.trim().isEmpty()) {
            this.filteredBillList = new ArrayList<>(this.fullBillList);
            notifyUi();
            return;
        }

        List<BillReportDto> tempFilteredList = new ArrayList<>();
        String cleanQuery = text.toLowerCase().trim();

        for (BillReportDto item : this.fullBillList) {
            if ((item.getTenantName() != null && item.getTenantName().toLowerCase().contains(cleanQuery)) ||
                    (item.getRoomNumber() != null && item.getRoomNumber().contains(cleanQuery))) {
                tempFilteredList.add(item);
            }
        }

        this.filteredBillList = tempFilteredList;
        notifyUi();
    }

    /**
     * Directly pushes adjusted calculations into storage cleanly inside background execution pools
     */
    /**
     * Pushes adjusted calculations into storage cleanly using inline database updates.
     */
    public void updateExistingBillMetrics(int billId, double currentReading, double rate, double fixed,double extra, double total, String note) {
        this.isLoading = true;
        this.isOperationSuccess = false;
        this.errorMessage = null;
        notifyUi();

        new Thread(() -> {
            try {
                // Open transaction boundary
                DatabaseConnection.beginTransaction();

                // 1. Fetch and update the Bill Aggregate entry
                Bill bill =DaoManager.getBillDao().getBillById(billId)
                        .orElseThrow(() -> new java.sql.SQLException("Bill target entity reference point missing."));

                bill.setTotalAmount(total);
                bill.setRatePerUnit(rate);
                bill.setFixedCharge(fixed);
                bill.setExtraCharge(extra);
                bill.setNote(note);
                DaoManager.getBillDao().updateBill(bill);

                // 2. FIX: Direct transaction update to bypass missing DAO interface methods completely
                String sqlUpdateReading = "UPDATE MeterReading SET reading_value = ? WHERE reading_id = ?";
                boolean readingUpdated = DbUtils.executeUpdate(
                        sqlUpdateReading,
                        currentReading,
                        bill.getCurrentReadingId()
                );

                if (!readingUpdated) {
                    throw new java.sql.SQLException("Meter reading row failed to update in database mapping.");
                }

                // Secure persist down to disk
                DatabaseConnection.commitTransaction();
                this.isOperationSuccess = true;
            } catch (java.sql.SQLException e) {
                DatabaseConnection.rollbackTransaction();
                this.errorMessage = "Failed to modify utility parameters: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }
}