package com.submeter.presentation.viewmodel;

import com.submeter.commonlib.dto.BillDTO;
import com.submeter.service.MeterBillingService;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends BaseViewModel{
    private final MeterBillingService meterBillingService;

    // Simple primitive UI layout states
    private boolean isDataLoaded = false;
    private boolean isStatusUpdateSuccess = false;

    // Analytical metrics parameters
    private double totalUnits = 0.0;
    private int totalBillsCount = 0;
    private double totalRevenue = 0.0;

    // Core list collections cached safely across fragment lifecycles
    private List<BillDTO> pendingBillsList = new ArrayList<>();


    public DashboardViewModel(MeterBillingService meterBillingService) {
        this.meterBillingService = meterBillingService;
    }

    public boolean isDataLoaded() { return isDataLoaded; }
    public boolean isStatusUpdateSuccess() { return isStatusUpdateSuccess; }
    public double getTotalUnits() { return totalUnits; }
    public int getTotalBillsCount() { return totalBillsCount; }
    public double getTotalRevenue() { return totalRevenue; }
    public List<BillDTO> getPendingBillsList() { return pendingBillsList; }

    // --- UI Actions ---

    public void loadDashboardSummary() {

        setLoading(true);
        setErrorMessage( null );
        this.isDataLoaded = false;
        notifyStateChanged();

        new Thread(() -> {
            try {
                // 1. Gather master stats
                List<BillDTO> allBills = meterBillingService.getAllBillsReport();
                double tempUnits = 0.0;
                double tempRevenue = 0.0;

                if (allBills != null) {
                    for (BillDTO bill : allBills) {
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
                List<BillDTO> pending = meterBillingService.getAllPendingBills();
                this.pendingBillsList = pending != null ? new ArrayList<>(pending) : new ArrayList<>();

                this.isDataLoaded = true;
            } catch (Exception e) {
                setErrorMessage("Failed to load dashboard metrics: " + e.getMessage());
            } finally {
                setLoading(false);
                notifyStateChanged();
            }
        }).start();
    }

    public void changeBillBalance(BillDTO bill) {
        if (bill == null) return;

        setLoading(true);
        setErrorMessage( null );
        this.isStatusUpdateSuccess = false;
        notifyStateChanged();

        new Thread(() -> {
            try {

                // 1. Fire database update logic routines
                meterBillingService.updateBillPaymentDate(bill.getId(), "add date here");


                // 3. Evict from pending views instantly if marked paid
                int balance = 0;

                if (balance == 0) {
                } else {
                    this.pendingBillsList.removeIf(item -> item.getId() == bill.getId());
                }

                // Recalculate summary metrics automatically via a background refresh cycle
                refreshStats();
                this.isStatusUpdateSuccess = true;
            } catch (Exception e) {
                setErrorMessage( "Status Mutation Failure: " + e.getMessage() );
            } finally {
                setLoading(false);
                notifyStateChanged();
            }
        }).start();
    }

    void refreshStats() throws Exception{

        // Recalculate summary metrics automatically via a background refresh cycle
        List<BillDTO> allBills = meterBillingService.getAllBillsReport();
        double tempUnits = 0.0;
        double tempRevenue = 0.0;
        if (allBills != null) {
            for (BillDTO b : allBills) {
                tempUnits += (b.getCurrentReading() - b.getPreviousReading());
                tempRevenue += b.getTotalAmount();
            }
            this.totalBillsCount = allBills.size();
        }
        this.totalUnits = tempUnits;
        this.totalRevenue = tempRevenue;

        this.isStatusUpdateSuccess = true;
    }
}
