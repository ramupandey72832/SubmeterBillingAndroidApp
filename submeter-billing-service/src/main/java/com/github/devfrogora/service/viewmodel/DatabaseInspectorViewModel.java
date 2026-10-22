package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.data.dao.DaoManager;
import com.submeter.data.entities.Bill;
//import com.github.devfrogora.data.entities.MeterReading;
import com.submeter.data.entities.Room;
import com.submeter.data.entities.Submeter;
import com.submeter.data.entities.Tenant;
import com.submeter.data.entities.MeterReading;
//import com.github.devfrogora.data.entities.Submeter;
import com.submeter.data.entities.SubmeterAllocation;
import com.submeter.data.entities.Tenancy;
//import com.github.devfrogora.data.entities.Tenant;
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
                    currentHeaders.addAll(Arrays.asList("Room ID", "Room Number", "Room Type"));
                    for (Room r : DaoManager.getRoomDao().getAllRooms()) {
                        currentRows.add(Arrays.asList(String.valueOf(r.getId()), r.getRoomNumber(), r.getRoomType()));
                    }
                    break;

                case "TENANTS":
                    currentHeaders.addAll(Arrays.asList("Tenant ID", "Name", "Aadhaar", "Mobile", "Address"));
                    for (Tenant t : DaoManager.getTenantDao().getAllTenants()) {
                        currentRows.add(Arrays.asList(String.valueOf(t.getId()), t.getName(), t.getAadhaarNumber(), t.getPhoneNumber(), t.getAddress()));
                    }
                    break;

                case "TENANCIES":
                    currentHeaders.addAll(Arrays.asList("Tenancy ID", "Room ID", "Tenant ID", "Start Date", "End Date", "Is Active"));
                    for (Tenancy ty : DaoManager.getTenancyDao().getAllTenancies()) {
                        currentRows.add(Arrays.asList(String.valueOf(ty.getId()), String.valueOf(ty.getRoomId()),
                                String.valueOf(ty.getTenantId()), ty.getStartDate(),
                                ty.getEndDate() != null ? ty.getEndDate() : "--", ty.getEndDate() == null ? "YES" : "NO"));
                    }
                    break;

                case "SUBMETERS":
                    currentHeaders.addAll(Arrays.asList("Meter ID", "Room ID", "Serial Number", "Initial Reading"));
                    // Safe fallback map handling if tables contain raw option arrays
                    List<Submeter> allSubmeters = DaoManager.getSubmeterDao().getAllSubmeters();
                    for (Submeter s : allSubmeters) {
                        currentRows.add(Arrays.asList(String.valueOf(s.getId()), String.valueOf("s.getRoomId()"),
                                s.getMeterSerialNumber(), s.getInitialReading() + " kWh"));
                    }
                    break;

                case "SUBMETERS_ALLOCATION":
                    currentHeaders.addAll(Arrays.asList("allocation_id","meter_id", "room_id", "start_date", "end_date"));
                    // Safe fallback map handling if tables contain raw option arrays
                    List<SubmeterAllocation> submeterJunctions = DaoManager.getSubmeterAllocationDao().getAllAllocations();
                    for (SubmeterAllocation s : submeterJunctions) {
                        currentRows.add(Arrays.asList(String.valueOf(s.getId()), String.valueOf(s.getMeterId()),
                                String.valueOf(s.getRoomId()), s.getStartingDate() ,s.getEndingDate()));
                    }
                    break;

                case "BILLS":
                    currentHeaders.addAll(Arrays.asList("bill_id","previous_reading_id","current_reading_id","meter_id","meter_serial_number",
                            "tenant_id","tenant_name","room_number","units_consumed",
                            "rate_per_unit", "fixed_charge","extra_charge", "total_amount", "note", "billing_date"));
                    for (Bill b : DaoManager.getBillDao().getAllBills()) {
                        currentRows.add(Arrays.asList(String.valueOf(b.getId()), String.valueOf(b.getPreviousReadingId()),
                                String.valueOf(b.getCurrentReadingId()), String.valueOf(b.getMeterId()), b.getMeterSerialNumber(),
                                String.valueOf(b.getTenantId()), b.getTenantName(), b.getRoomNumber(), String.valueOf(b.getUnitsConsumed()),
                                String.valueOf(b.getRatePerUnit()), String.valueOf(b.getFixedCharge()),String.valueOf(b.getExtraCharge()),
                                 "₹" + b.getTotalAmount(), b.getNote(), b.getBillingDate()));
                    }
                    break;

                case "METER_READINGS":
                    currentHeaders.addAll(Arrays.asList("Reading ID", "Meter ID", "Reading Value", "Reading_date"));
                    for (MeterReading mr : DaoManager.getMeterReadingDao().getAllReadings()) {
                        currentRows.add(Arrays.asList(String.valueOf(mr.getId()), String.valueOf(mr.getMeterId()), mr.getReadingValue() + " kWh", mr.getDate()));
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