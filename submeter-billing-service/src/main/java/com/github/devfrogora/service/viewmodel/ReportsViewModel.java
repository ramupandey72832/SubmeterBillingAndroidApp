// File: submeter-billing-service/.../service/viewmodel/ReportsViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.utils.ExcelGenerator;
import com.github.devfrogora.service.utils.OperationResult;

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
                List<RoomRegistryDto> rooms = null;
                List<BillReportDto> bills = new ArrayList<>();
                OperationResult<List<RoomRegistryDto>> result = roomMeterService.getAllRoomReport();

                if (result.isSuccess()) {
                    // Post payload cleanly to the main UI thread recycler data observers
                    rooms = result.getData();
                } else {
                    // Post error messages to a single-use action live event to display a Toast or Snackbar
                    errorMessage = result.getMessage();
                }


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

    private boolean isRangeExcelWritten = false;

    public boolean isRangeExcelWritten() { return isRangeExcelWritten; }

    public void writeRangeExcelReport(java.io.OutputStream outputStream) {
        this.isLoading = true;
        this.isRangeExcelWritten = false;
        notifyUi();

        new Thread(() -> {
            try (outputStream) {
                var result = ExcelGenerator.generateBillReport(outputStream, this.rangeFilteredBills);
                if (result.isSuccess()) {
                    this.isRangeExcelWritten = true;
                } else {
                    this.errorMessage = result.getMessage();
                }
            } catch (Exception e) {
                this.errorMessage = "Stream write error: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    private boolean isBackupSuccess = false;
    private boolean isImportSuccess = false;
    private String operationResultMessage = null;

    public boolean isBackupSuccess() { return isBackupSuccess; }
    public boolean isImportSuccess() { return isImportSuccess; }
    public String getOperationResultMessage() { return operationResultMessage; }

    public void executeFullExcelBackup(java.io.OutputStream outputStream) {
        this.isLoading = true;
        this.errorMessage = null;
        this.isBackupSuccess = false;
        notifyUi();

        new Thread(() -> {
            try (outputStream) { // Use try-with-resources to ensure stream closures
                var result = ExcelGenerator.exportAllTablesToExcel(outputStream);
                if (result.isSuccess()) {
                    this.operationResultMessage = "Database successfully backed up to Excel workbook!";
                    this.isBackupSuccess = true;
                } else {
                    this.errorMessage = result.getMessage();
                }
            } catch (Exception e) {
                this.errorMessage = "Stream error during backup: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    public void executeFullExcelImport(java.io.InputStream inputStream) {
        this.isLoading = true;
        this.errorMessage = null;
        this.isImportSuccess = false;
        notifyUi();

        new Thread(() -> {
            try (inputStream) {
                var result = ExcelGenerator.importAllTablesFromExcel(inputStream);
                if (result.isSuccess()) {
                    this.operationResultMessage = "Database successfully restored from Excel workbook!";
                    this.isImportSuccess = true;
                    loadHistoricalThreeMonthReports();
                } else {
                    this.errorMessage = result.getMessage();
                }
            } catch (Exception e) {
                this.errorMessage = "Stream error during import: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }
}