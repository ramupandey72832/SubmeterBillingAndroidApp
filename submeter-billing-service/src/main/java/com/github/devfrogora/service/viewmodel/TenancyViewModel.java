// File: submeter-billing-service/.../service/viewmodel/TenancyViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.dto.TenancyDTO;
import com.github.devfrogora.service.dto.SubmeterDTO;
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.utils.OperationResult;

import java.util.Optional;

public class TenancyViewModel {

    private final TenancyManagementService tenancyService;
    private final RoomMeterService roomMeterService;
    private final MeterBillingService meterService;

    // Direct UI State Fields
    private boolean isLoading = false;
    private String errorMessage = null;
    private boolean isTerminationSuccess = false;

    // Fetched Detail Data Fields
    private TenantDTO loadedTenant = null;
    private double latestMeterReading = 0.0;
    private boolean detailsLoaded = false;

    public interface StateListener {
        void onStateChanged();
    }

    private StateListener listener;

    public TenancyViewModel(TenancyManagementService tenancyService, RoomMeterService roomMeterService, MeterBillingService meterService) {
        this.tenancyService = tenancyService;
        this.roomMeterService = roomMeterService;
        this.meterService = meterService;
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
    public boolean isTerminationSuccess() { return isTerminationSuccess; }
    public TenantDTO getLoadedTenant() { return loadedTenant; }
    public double getLatestMeterReading() { return latestMeterReading; }
    public boolean areDetailsLoaded() { return detailsLoaded; }

    // --- UI Actions ---

    public void verifyAndFetchActiveTenant(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            this.errorMessage = "Please enter a valid room number.";
            notifyUi();
            return;
        }

        this.isLoading = true;
        this.errorMessage = null;
        this.detailsLoaded = false;
        notifyUi();

        new Thread(() -> {
            try {
                TenancyDTO tenancyDTO = tenancyService.findActiveTenancyByRoomNumber(roomNumber.trim());
                if (tenancyDTO == null) {
                    this.errorMessage = "No active tenancy found for Room " + roomNumber;
                    this.isLoading = false;
                    notifyUi();
                    return;
                }

                Optional<TenantDTO> tenantOpt = tenancyService.findTenantByAadhar(tenancyDTO.getTenantAaddhar());
                if (tenantOpt.isEmpty()) {
                    this.errorMessage = "Active tenant records are missing for this tenancy profile.";
                    this.isLoading = false;
                    notifyUi();
                    return;
                }

                // Gather submeter details using the modern Optional signature
                Optional<SubmeterDTO> submeterOpt = roomMeterService.getSubmeterByRoomNumber(roomNumber.trim());
                if (submeterOpt.isPresent()) {
                    this.latestMeterReading = meterService.getLatestReading(submeterOpt.get().getMeterSerialNumber());
                } else {
                    this.latestMeterReading = 0.0;
                }

                this.loadedTenant = tenantOpt.get();
                this.detailsLoaded = true;

            } catch (Exception e) {
                this.errorMessage = "Error querying tenant information: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    public void terminateTenancy(String roomNumber, String endDate) {
        if (roomNumber == null || roomNumber.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            this.errorMessage = "Room number and end date are mandatory fields.";
            notifyUi();
            return;
        }

        this.isLoading = true;
        this.errorMessage = null;
        this.isTerminationSuccess = false;
        notifyUi();

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
                notifyUi();
            }
        }).start();
    }
}