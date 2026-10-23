package com.submeter.presentation.viewmodel;

import com.submeter.commonlib.dto.BillDTO;
import com.submeter.service.impl.core.BillService;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends BaseViewModel {

    private final BillService billingService;

    private boolean isDataLoaded;
    private boolean isStatusUpdateSuccess;

    private double totalUnits;
    private int totalBillsCount;
    private double totalBilledAmount;

    private List<BillDTO> pendingBillsList = new ArrayList<>();

    public DashboardViewModel(BillService billingService) {
        this.billingService = billingService;
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    public boolean isStatusUpdateSuccess() {
        return isStatusUpdateSuccess;
    }

    public double getTotalUnits() {
        return totalUnits;
    }

    public int getTotalBillsCount() {
        return totalBillsCount;
    }

    public double getTotalBilledAmount() {
        return totalBilledAmount;
    }

    public List<BillDTO> getPendingBillsList() {
        return new ArrayList<>(pendingBillsList);
    }

    public void loadDashboardSummary() {

        setLoading(true);
        setErrorMessage(null);
        isDataLoaded = false;
        notifyStateChanged();

        new Thread(() -> {
            try {
                refreshStats();

                isDataLoaded = true;

            } catch (Exception e) {
                setErrorMessage(
                        "Failed to load dashboard metrics: " + e.getMessage()
                );
            } finally {
                setLoading(false);
                notifyStateChanged();
            }
        }).start();
    }

    private void refreshStats() throws Exception {

        List<BillDTO> allBills = billingService.getAll();

        double units = 0.0;
        double billedAmount = 0.0;
        int billCount = 0;

        if (allBills != null) {
            for (BillDTO bill : allBills) {

                units += bill.getCurrentReading()
                        - bill.getPreviousReading();

                billedAmount += bill.getTotalAmount();

                billCount++;
            }
        }

        List<BillDTO> pendingBills =
                billingService.getAllPending();

        this.totalUnits = units;
        this.totalBilledAmount = billedAmount;
        this.totalBillsCount = billCount;

        this.pendingBillsList = pendingBills == null
                ? new ArrayList<>()
                : new ArrayList<>(pendingBills);
    }

}