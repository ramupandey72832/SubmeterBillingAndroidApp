// File: submeter-billing-service/.../service/viewmodel/ReportsViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsViewModel {

    private final MeterBillingService meterBillingService;
    private final RoomMeterService roomMeterService;

    // Direct primitive UI layout states
    private boolean isLoading = false;
    private String errorMessage = null;

    // Concrete targets exposed directly for views to pull from
    private Map<String, List<BillReportDto>> groupedPreviousReports = new LinkedHashMap<>();
    private List<BillReportDto> rangeFilteredBills = new ArrayList<>();
    private List<BillReportDto> latestMonthlyBills = new ArrayList<>();

    // Action execution completion flags
    private boolean isHistoryLoaded = false;
    private boolean isRangeExportReady = false;
    private boolean isMultiPdfReady = false;

    public interface StateListener {
        void onStateChanged();
    }

    private StateListener listener;

    public ReportsViewModel(MeterBillingService meterBillingService, RoomMeterService roomMeterService) {
        this.meterBillingService = meterBillingService;
        this.roomMeterService = roomMeterService;
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
    public Map<String, List<BillReportDto>> getGroupedPreviousReports() { return groupedPreviousReports; }
    public List<BillReportDto> getRangeFilteredBills() { return rangeFilteredBills; }
    public List<BillReportDto> getLatestMonthlyBills() { return latestMonthlyBills; }
    public boolean isHistoryLoaded() { return isHistoryLoaded; }
    public boolean isRangeExportReady() { return isRangeExportReady; }
    public boolean isMultiPdfReady() { return isMultiPdfReady; }

    // --- UI Presentation Actions ---

    public void loadHistoricalThreeMonthReports() {
        this.isLoading = true;
        this.errorMessage = null;
        this.isHistoryLoaded = false;
        notifyUi();

        new Thread(() -> {
            try {
                List<BillReportDto> lastThreeMonthBills = meterBillingService.getLatestThreeMonthBills();

                if (lastThreeMonthBills != null && !lastThreeMonthBills.isEmpty()) {
                    // Group them chronologically ("yyyy-MM" -> List of bills) in Business Core
                    this.groupedPreviousReports = lastThreeMonthBills.stream()
                            .collect(Collectors.groupingBy(
                                    bill -> bill.getBillingDate().substring(0, 7),
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));
                } else {
                    this.groupedPreviousReports = new LinkedHashMap<>();
                }
                this.isHistoryLoaded = true;
            } catch (Exception e) {
                this.errorMessage = "Failed to load past histories: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    public void fetchBillsByRange(String start, String end) {
        this.isLoading = true;
        this.errorMessage = null;
        this.isRangeExportReady = false;
        notifyUi();

        new Thread(() -> {
            try {
                List<BillReportDto> filtered = meterBillingService.getBillsByRange(start, end);
                this.rangeFilteredBills = filtered != null ? filtered : new ArrayList<>();
                this.isRangeExportReady = true;
            } catch (Exception e) {
                this.errorMessage = "Error filtering date bounds: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    public void compileLatestMonthlyBills() {
        this.isLoading = true;
        this.errorMessage = null;
        this.isMultiPdfReady = false;
        notifyUi();

        new Thread(() -> {
            try {
                List<RoomRegistryDto> rooms = roomMeterService.getAllRoomReport();
                List<BillReportDto> bills = new ArrayList<>();

                if (rooms != null) {
                    for (RoomRegistryDto room : rooms) {
                        try {
                            BillReportDto latestBill = meterBillingService.getLatestBill(room.getRoomNumber());
                            if (latestBill != null) {
                                bills.add(latestBill);
                            }
                        } catch (Exception ignored) {
                            // Skip a room if query fails without crashing the whole collection loop
                        }
                    }
                }
                this.latestMonthlyBills = bills;
                this.isMultiPdfReady = true;
            } catch (Exception e) {
                this.errorMessage = "Error gathering active targets: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }
}