// File: submeter-billing-service/.../service/viewmodel/MeterReadingViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.dto.TenancyDTO;
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.dto.SubmeterDTO;
import com.github.devfrogora.service.utils.OperationResult;

import java.util.Optional;

public class MeterReadingViewModel {

    private final RoomMeterService roomMeterService;
    private final MeterBillingService meterBillingService;
    private final TenancyManagementService tenancyManagementService;

    // Simple primitive UI State fields
    private boolean isLoading = false;
    private String errorMessage = null;
    private boolean isRoomVerified = false;

    // Mapped business payload targets
    private String meterSerialNumber = "";
    private double previousReading = 0.0;
    private String tenantName = "";

    public interface StateListener {
        void onStateChanged();
    }

    private StateListener listener;

    public MeterReadingViewModel(RoomMeterService roomMeterService,
                                 MeterBillingService meterBillingService,
                                 TenancyManagementService tenancyManagementService) {
        this.roomMeterService = roomMeterService;
        this.meterBillingService = meterBillingService;
        this.tenancyManagementService = tenancyManagementService;
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
    public boolean isRoomVerified() { return isRoomVerified; }
    public String getMeterSerialNumber() { return meterSerialNumber; }
    public double getPreviousReading() { return previousReading; }
    public String getTenantName() { return tenantName; }

    // --- UI Presentation Actions ---

    public void checkRoomDetails(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            this.errorMessage = "Validation Error: Room number cannot be blank.";
            notifyUi();
            return;
        }

        this.isLoading = true;
        this.errorMessage = null;
        this.isRoomVerified = false;
        notifyUi();

        new Thread(() -> {
            try {
                // 1. Resolve hardware submeter registration payload using the Optional layout
                OperationResult<SubmeterDTO> submeterOpt = roomMeterService.getSubmeterByRoomNumber(roomNumber.trim());
                if (!submeterOpt.isSuccess()) {
                    this.errorMessage = "Asset Error: No deployed submeter found for room " + roomNumber;
                    this.isLoading = false;
                    notifyUi();
                    return;
                }
                SubmeterDTO submeter = submeterOpt.getData();

                // 2. Resolve active leasing bindings
                TenancyDTO tenancy = tenancyManagementService.findActiveTenancyByRoomNumber(roomNumber.trim());
                if (tenancy == null) {
                    this.errorMessage = "Lease Error: There is no active tenant assigned to room " + roomNumber;
                    this.isLoading = false;
                    notifyUi();
                    return;
                }

                // 3. Hydrate matching occupant demographics
                Optional<TenantDTO> tenantOpt = tenancyManagementService.findTenantByAadhar(tenancy.getTenantAaddhar());
                if (tenantOpt.isEmpty()) {
                    this.errorMessage = "Data Inconsistency: Active occupant records are missing from system tracking.";
                    this.isLoading = false;
                    notifyUi();
                    return;
                }

                // 4. Retrieve historical metric logs from storage engines
                this.previousReading = meterBillingService.getLatestReading(submeter.getMeterSerialNumber());
                this.meterSerialNumber = submeter.getMeterSerialNumber();
                this.tenantName = tenantOpt.get().getName();
                this.isRoomVerified = true;

            } catch (Exception e) {
                this.errorMessage = "Critical Verification Failure: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }
}