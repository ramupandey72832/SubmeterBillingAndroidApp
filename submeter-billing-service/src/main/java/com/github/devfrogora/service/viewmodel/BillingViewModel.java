// File: submeter-billing-service/.../service/viewmodel/BillingViewModel.java
package com.github.devfrogora.service.viewmodel;

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
}