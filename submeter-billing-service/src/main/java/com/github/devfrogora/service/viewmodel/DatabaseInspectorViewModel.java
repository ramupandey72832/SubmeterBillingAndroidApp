package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.*;
import com.github.devfrogora.service.utils.OperationResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseInspectorViewModel {

    public interface StateListener {
        void onStateChanged();
    }

    private StateListener stateListener;
    private List<String> currentHeaders = new ArrayList<>();
    private List<List<String>> currentRows = new ArrayList<>();
    private String errorMessage = null;
    private boolean isLoading = false;

    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    private void notifyStateChanged() {
        if (stateListener != null) stateListener.onStateChanged();
    }

    /**
     * Core Architectural Data Mapping Loop: Maps individual entity schemas
     * safely into simple tabular row lists of strings for the view layer.
     */
    public void loadTableData(String tableName) {
        this.isLoading = true;
        this.errorMessage = null;
        this.currentHeaders.clear();
        this.currentRows.clear();
        notifyStateChanged();

        try {
            switch (tableName.toUpperCase()) {
                case "ROOMS":
                    currentHeaders.addAll(Arrays.asList("Room ID", "Room Number", "Room Type", "Rent Amount"));
                    for (Room r : DaoManager.getRoomDao().getAllRooms()) {
                        currentRows.add(Arrays.asList(String.valueOf(r.getRoomId()), r.getRoomNumber(), r.getRoomType(), "₹" + r.getRentAmount()));
                    }
                    break;

                case "TENANTS":
                    currentHeaders.addAll(Arrays.asList("Tenant ID", "Name", "Aadhaar", "Mobile", "Address"));
                    for (Tenant t : DaoManager.getTenantDao().getAllTenants()) {
                        currentRows.add(Arrays.asList(String.valueOf(t.getTenantId()), t.getName(), t.getAadharNumber(), t.getPhoneNumber(), t.getAddress()));
                    }
                    break;

                case "TENANCIES":
                    currentHeaders.addAll(Arrays.asList("Tenancy ID", "Room ID", "Tenant ID", "Start Date", "End Date", "Is Active"));
                    for (Tenancy ty : DaoManager.getTenancyDao().getAllTenancies()) {
                        currentRows.add(Arrays.asList(String.valueOf(ty.getTenancyId()), String.valueOf(ty.getRoomId()), String.valueOf(ty.getTenantId()), ty.getStartDate(), ty.getEndDate() != null ? ty.getEndDate() : "--", ty.getEndDate() == null ? "YES" : "NO"));
                    }
                    break;

                case "SUBMETERS":
                    currentHeaders.addAll(Arrays.asList("Meter ID", "Room ID", "Serial Number", "Initial Reading"));
                    // Safe fallback map handling if tables contain raw option arrays
                    List<Room> allRooms = DaoManager.getRoomDao().getAllRooms();
                    for (Room r : allRooms) {
                        DaoManager.getSubmeterDao().getSubmeterByRoomId(r.getRoomId()).ifPresent(s -> {
                            currentRows.add(Arrays.asList(String.valueOf(s.getMeterId()), String.valueOf(s.getRoomId()), s.getMeterSerialNumber(), s.getInitialReading() + " kWh"));
                        });
                    }
                    break;

                case "BILLS":
                    currentHeaders.addAll(Arrays.asList("bill_id","previous_reading_id","current_reading_id","meter_id","meter_serial_number",
                            "tenant_id","tenant_name","room_number","units_consumed",
                            "rate_per_unit", "fixed_charge","extra_charge", "total_amount", "note", "billing_date", "payment_date", "paid"));
                    for (Bill b : DaoManager.getBillDao().getAllBills()) {
                        currentRows.add(Arrays.asList(String.valueOf(b.getBillId()), String.valueOf(b.getPreviousReadingId()),
                                String.valueOf(b.getCurrentReadingId()), String.valueOf(b.getMeterId()), b.getMeterSerialNumber(),
                                String.valueOf(b.getTenantId()), b.getTenantName(), b.getRoomNumber(), String.valueOf(b.getUnitsConsumed()),
                                String.valueOf(b.getRatePerUnit()), String.valueOf(b.getFixedCharge()),String.valueOf(b.getExtraCharge()),
                                 "₹" + b.getTotalAmount(), b.getNote(), b.getBillingDate(), b.getPaymentDate(),b.isPaid() ? "PAID" : "UNPAID"));
                    }
                    break;

                case "METER_READINGS":
                    currentHeaders.addAll(Arrays.asList("Reading ID", "Meter ID", "Reading Value", "Reading_date"));
                    for (MeterReading mr : DaoManager.getMeterReadingDao().getAllReadings()) {
                        currentRows.add(Arrays.asList(String.valueOf(mr.getReadingId()), String.valueOf(mr.getMeterId()), mr.getReadingValue() + " kWh", mr.getReadingDate()));
                    }
                    break;

                default:
                    this.errorMessage = "Unknown Table Target context mapping ruleset.";
            }
        } catch (SQLException e) {
            this.errorMessage = "Database Inspection Failure: " + e.getMessage();
        } finally {
            this.isLoading = false;
            notifyStateChanged();
        }
    }

    // --- State Access Getters ---
    public List<String> getCurrentHeaders() { return currentHeaders; }
    public List<List<String>> getCurrentRows() { return currentRows; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isLoading() { return isLoading; }
}