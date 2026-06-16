// File: submeter-billing-service/.../service/viewmodel/TenancyViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.Room;
import com.github.devfrogora.data.entities.Tenancy;
import com.github.devfrogora.data.entities.Tenant;
import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.dto.TenancyDTO;
import com.github.devfrogora.service.dto.SubmeterDTO;
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.utils.OperationResult;

import java.sql.SQLException;
import java.util.Optional;

public class TenancyViewModel {

    private final TenancyManagementService tenancyService;
    private final RoomMeterService roomMeterService;
    private final MeterBillingService meterService;

    // Direct UI State Fields
    private boolean isLoading = false;
    private String errorMessage = null;

    // Decoupled architecture data flags
    private boolean isOperationSuccess = false;
    private String feedbackMessage = null;

    private boolean isTerminationSuccess = false;

    // Fetched Detail Data Fields
    private TenantDTO loadedTenant = null;
    private double latestMeterReading = 0.0;
    private boolean detailsLoaded = false;

    // Holds currently loaded tenant details for form binding
    private TenantDTO currentEditingTenant = null;
    private int internalTenantId = -1;

    public interface StateListener {
        void onStateChanged();
    }

    private StateListener stateListener;

    public TenancyViewModel(TenancyManagementService tenancyService, RoomMeterService roomMeterService, MeterBillingService meterService) {
        this.tenancyService = tenancyService;
        this.roomMeterService = roomMeterService;
        this.meterService = meterService;
    }

    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }


    private void notifyStateChanged() {
        if (stateListener != null) {
            stateListener.onStateChanged();
        }
    }

    // --- State Getters ---
    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isTerminationSuccess() { return isTerminationSuccess; }
    public TenantDTO getLoadedTenant() { return loadedTenant; }
    public double getLatestMeterReading() { return latestMeterReading; }
    public boolean areDetailsLoaded() { return detailsLoaded; }

    // --- UI Actions ---

    public void verifyAndFetchActiveTenant(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            this.errorMessage = "Please enter a valid room number.";
            notifyStateChanged();
            return;
        }

        this.isLoading = true;
        this.errorMessage = null;
        this.detailsLoaded = false;
        notifyStateChanged();

        new Thread(() -> {
            try {
                TenancyDTO tenancyDTO = tenancyService.findActiveTenancyByRoomNumber(roomNumber.trim());
                if (tenancyDTO == null) {
                    this.errorMessage = "No active tenancy found for Room " + roomNumber;
                    this.isLoading = false;
                    notifyStateChanged();
                    return;
                }

                Optional<TenantDTO> tenantOpt = tenancyService.findTenantByAadhar(tenancyDTO.getTenantAaddhar());
                if (tenantOpt.isEmpty()) {
                    this.errorMessage = "Active tenant records are missing for this tenancy profile.";
                    this.isLoading = false;
                    notifyStateChanged();
                    return;
                }

                // Gather submeter details using the modern Optional signature
                OperationResult<SubmeterDTO> submeterOpt = roomMeterService.getSubmeterByRoomNumber(roomNumber.trim());
                if (submeterOpt.isSuccess()) {
                    this.latestMeterReading = meterService.getLatestReading(submeterOpt.getData().getMeterSerialNumber());
                } else {
                    this.latestMeterReading = 0.0;
                }

                this.loadedTenant = tenantOpt.get();
                this.detailsLoaded = true;

            } catch (Exception e) {
                this.errorMessage = "Error querying tenant information: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyStateChanged();
            }
        }).start();
    }

    public void terminateTenancy(String roomNumber, String endDate) {
        if (roomNumber == null || roomNumber.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            this.errorMessage = "Room number and end date are mandatory fields.";
            notifyStateChanged();
            return;
        }

        this.isLoading = true;
        this.errorMessage = null;
        this.isTerminationSuccess = false;
        notifyStateChanged();

        new Thread(() -> {
            try {
                // Adjust your tenancy service method to return OperationResult or safely catch errors
                tenancyService.terminateTenancyOfRoom(roomNumber.trim(), endDate.trim());
                this.isTerminationSuccess = true;
                this.detailsLoaded = false; // Hide the details container after processing
            } catch (Exception e) {
                this.errorMessage = e.getMessage();
            } finally {
                this.isLoading = false;
                notifyStateChanged();
            }
        }).start();
    }

    /**
     * Fetches current tenant profile details by Room Number asynchronously
     */
    public void loadTenantForRoom(String roomNumber) {
        this.isLoading = true;
        this.feedbackMessage = null;
        this.currentEditingTenant = null;
        this.internalTenantId = -1;
        notifyStateChanged();

        try {
            Optional<Room> roomOpt = DaoManager.getRoomDao().getRoomByNumber(roomNumber);
            if (roomOpt.isPresent()) {
                Optional<Tenancy> activeLease = DaoManager.getTenancyDao().getActiveTenancyByRoomId(roomOpt.get().getRoomId());
                if (activeLease.isPresent()) {
                    Optional<Tenant> tenantOpt = DaoManager.getTenantDao().getTenantById(activeLease.get().getTenantId());
                    if (tenantOpt.isPresent()) {
                        Tenant tenant = tenantOpt.get();
                        this.internalTenantId = tenant.getTenantId();

                        // Map the internal data entity cleanly into an app-visible DTO package
                        this.currentEditingTenant = new TenantDTO(
                                tenant.getName(),
                                tenant.getAadharNumber(),
                                tenant.getPhoneNumber()
                        );
                        this.isOperationSuccess = true;
                    } else {
                        this.feedbackMessage = "Tenant identity profile record missing.";
                    }
                } else {
                    this.feedbackMessage = "No active occupancy found for Room " + roomNumber;
                }
            } else {
                this.feedbackMessage = "Room details not found.";
            }
        } catch (SQLException e) {
            this.feedbackMessage = "Database Fetch Failure: " + e.getMessage();
        } finally {
            this.isLoading = false;
            notifyStateChanged();
        }
    }

    /**
     * Updates tenant profile data safely without breaking transactional keys
     */
    public void quickUpdateTenantProfile(String name, String mobile, String aaddharNumber, String address) {
        if (internalTenantId == -1) {
            this.feedbackMessage = "No active profile loaded to update.";
            notifyStateChanged();
            return;
        }

        this.isLoading = true;
        this.isOperationSuccess = false;
        this.feedbackMessage = null;
        notifyStateChanged();

        try {
            Tenant tenant = DaoManager.getTenantDao().getTenantById(internalTenantId)
                    .orElseThrow(() -> new SQLException("Tenant record mapping not found."));

            // Safely mutate profile parameters
            tenant.setName(name);
            tenant.setPhoneNumber(mobile);
            tenant.setAddress(address);
            tenant.setAadharNumber(aaddharNumber);

            boolean isUpdated = DaoManager.getTenantDao().updateTenant(tenant);
            if (isUpdated) {
                this.isOperationSuccess = true;
                this.currentEditingTenant = null; // Clear out state payload upon processing loop completion
                this.internalTenantId = -1;
                this.feedbackMessage = "Tenant details updated successfully.";
            } else {
                this.feedbackMessage = "Storage mutation request rejected.";
            }
        } catch (SQLException e) {
            this.feedbackMessage = "Database Write Failure: " + e.getMessage();
        } finally {
            this.isLoading = false;
            notifyStateChanged();
        }
    }
    // --- State Getters ---
    public boolean isOperationSuccess() { return isOperationSuccess; }
    public String getFeedbackMessage() { return feedbackMessage; }
    public TenantDTO getCurrentEditingTenant() { return currentEditingTenant; }
}