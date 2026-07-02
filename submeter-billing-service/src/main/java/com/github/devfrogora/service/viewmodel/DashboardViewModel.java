// File: submeter-billing-service/.../service/viewmodel/DashboardViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.dto.reports.BillReportDto;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel {

    private final MeterBillingService meterBillingService;

    // Simple primitive UI layout states
    private boolean isLoading = false;
    private String errorMessage = null;
    private boolean isDataLoaded = false;
    private boolean isStatusUpdateSuccess = false;

    // Analytical metrics parameters
    private double totalUnits = 0.0;
    private int totalBillsCount = 0;
    private double totalRevenue = 0.0;

    // Core list collections cached safely across fragment lifecycles
    private List<BillReportDto> pendingBillsList = new ArrayList<>();


    public interface StateListener {
        void onStateChanged();
    }

    private StateListener listener;

    public DashboardViewModel(MeterBillingService meterBillingService) {
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

    // --- State Getters ---
    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isDataLoaded() { return isDataLoaded; }
    public boolean isStatusUpdateSuccess() { return isStatusUpdateSuccess; }
    public double getTotalUnits() { return totalUnits; }
    public int getTotalBillsCount() { return totalBillsCount; }
    public double getTotalRevenue() { return totalRevenue; }
    public List<BillReportDto> getPendingBillsList() { return pendingBillsList; }

    // --- UI Actions ---

    public void loadDashboardSummary() {
        this.isLoading = true;
        this.errorMessage = null;
        this.isDataLoaded = false;
        notifyUi();

        new Thread(() -> {
            try {
                // 1. Gather master stats
                List<BillReportDto> allBills = meterBillingService.getAllBillsReport();
                double tempUnits = 0.0;
                double tempRevenue = 0.0;

                if (allBills != null) {
                    for (BillReportDto bill : allBills) {
                        tempUnits += (bill.getCurrentReading() - bill.getPreviousReading());
                        tempRevenue += bill.getTotalAmount();
                    }
                    this.totalBillsCount = allBills.size();
                } else {
                    this.totalBillsCount = 0;
                }

                this.totalUnits = tempUnits;
                this.totalRevenue = tempRevenue;

                // 2. Gather matching pending collections
                List<BillReportDto> pending = meterBillingService.getAllPendingBills();
                this.pendingBillsList = pending != null ? new ArrayList<>(pending) : new ArrayList<>();

                this.isDataLoaded = true;
            } catch (Exception e) {
                this.errorMessage = "Failed to load dashboard metrics: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    public void changeBillPaymentStatus(BillReportDto bill) {
        if (bill == null) return;

        this.isLoading = true;
        this.errorMessage = null;
        this.isStatusUpdateSuccess = false;
        notifyUi();

        new Thread(() -> {
            try {
                boolean currentIsPaid = bill.getPaymentStatus().equalsIgnoreCase("YES") ||
                        bill.getPaymentStatus().equalsIgnoreCase("PAID");
                boolean targetStatus = !currentIsPaid;

                // 1. Fire database update logic routines
                meterBillingService.updateBillPaymentStatus(bill.getBillId(), targetStatus);

                // 2. Adjust local list status models instantly
                bill.setPaymentStatus(targetStatus ? "PAID" : "UNPAID");

                // 3. Evict from pending views instantly if marked paid
                if (targetStatus) {
                    this.pendingBillsList.removeIf(item -> item.getBillId() == bill.getBillId());
                }

                // Recalculate summary metrics automatically via a background refresh cycle
                List<BillReportDto> allBills = meterBillingService.getAllBillsReport();
                double tempUnits = 0.0;
                double tempRevenue = 0.0;
                if (allBills != null) {
                    for (BillReportDto b : allBills) {
                        tempUnits += (b.getCurrentReading() - b.getPreviousReading());
                        tempRevenue += b.getTotalAmount();
                    }
                    this.totalBillsCount = allBills.size();
                }
                this.totalUnits = tempUnits;
                this.totalRevenue = tempRevenue;

                this.isStatusUpdateSuccess = true;
            } catch (Exception e) {
                this.errorMessage = "Status Mutation Failure: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    // Inside DashboardViewModel.java
    private String migrationStatus;

    public void performDatabaseCheck() {
        meterBillingService.checkAndRunMigrations(new MeterBillingService.MigrationCallback() {
            @Override
            public void onMessage(String msg) {
                migrationStatus = msg;
                notifyUi(); // Trigger UI update
            }

            @Override
            public void onError(String err, Exception e) {
                errorMessage = err + ": " + e.getMessage();
                notifyUi();
            }
        });
    }

    public String getMigrationStatus() { return migrationStatus; }
    public void clearMigrationStatus() {
        this.migrationStatus = null;
    }
}