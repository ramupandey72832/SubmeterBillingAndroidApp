package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.dto.TenancyDTO;
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.dto.SubmeterDTO;
import com.github.devfrogora.service.utils.OperationResult;

import java.util.Optional;
import java.util.function.Consumer;

public class MeterReadingViewModel {

    private final RoomMeterService roomMeterService;
    private final MeterBillingService meterBillingService;
    private final TenancyManagementService tenancyManagementService;

    // 1. UI State Holder Pattern: Single source of truth for the screen
    public static class UiState {
        public final boolean isLoading;
        public final String errorMessage;
        public final boolean isRoomVerified;
        public final String meterSerialNumber;
        public final double previousReading;
        public final String tenantName;
        public final String tenantContactNumber;

        public UiState(boolean isLoading, String errorMessage, boolean isRoomVerified,
                       String meterSerialNumber, double previousReading, String tenantName, String tenantContactNumber) {
            this.isLoading = isLoading;
            this.errorMessage = errorMessage;
            this.isRoomVerified = isRoomVerified;
            this.meterSerialNumber = meterSerialNumber;
            this.previousReading = previousReading;
            this.tenantName = tenantName;
            this.tenantContactNumber = tenantContactNumber;
        }

        public static UiState initial() {
            return new UiState(false, null, false, "", 0.0, "", "");
        }
    }

    private UiState currentState = UiState.initial();
    private Consumer<UiState> stateObserver;

    public MeterReadingViewModel(RoomMeterService roomMeterService,
                                 MeterBillingService meterBillingService,
                                 TenancyManagementService tenancyManagementService) {
        this.roomMeterService = roomMeterService;
        this.meterBillingService = meterBillingService;
        this.tenancyManagementService = tenancyManagementService;
    }

    // Register UI listener
    public void observeState(Consumer<UiState> observer) {
        this.stateObserver = observer;
        observer.accept(currentState); // Emit initial state immediately
    }

    private void updateState(UiState newState) {
        this.currentState = newState;
        if (stateObserver != null) {
            stateObserver.accept(currentState);
        }
    }

    // --- Clean Action Flow ---

    public void checkRoomDetails(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            updateState(new UiState(false, "Validation Error: Room number cannot be blank.", false, "", 0.0, "", ""));
            return;
        }

        String cleanRoom = roomNumber.trim();

        // Show Loading State
        updateState(new UiState(true, null, false, "", 0.0, "", ""));

        // Execute background check (Consider using an ExecutorService or Android Architecture Components)
        new Thread(() -> {
            try {
                // Step 1: Verify Submeter
                OperationResult<SubmeterDTO> submeterOpt = roomMeterService.getSubmeterByRoomNumber(cleanRoom);
                if (!submeterOpt.isSuccess()) {
                    emitError("Asset Error: No deployed submeter found for room " + cleanRoom);
                    return;
                }
                SubmeterDTO submeter = submeterOpt.getData();

                // Step 2: Verify Active Tenancy
                TenancyDTO tenancy = tenancyManagementService.findActiveTenancyByRoomNumber(cleanRoom);
                if (tenancy == null) {
                    emitError("Lease Error: There is no active tenant assigned to room " + cleanRoom);
                    return;
                }

                // Step 3: Verify Tenant Demographics
                Optional<TenantDTO> tenantOpt = tenancyManagementService.findTenantByAadhar(tenancy.getTenantAaddhar());
                if (tenantOpt.isEmpty()) {
                    emitError("Data Inconsistency: Active occupant records are missing.");
                    return;
                }

                // Step 4: Fetch Historical Data & Publish Success State
                double prevReading = meterBillingService.getLatestReading(submeter.getMeterSerialNumber());
                TenantDTO tenant = tenantOpt.get();

                updateState(new UiState(
                        false,
                        null,
                        true,
                        submeter.getMeterSerialNumber(),
                        prevReading,
                        tenant.getName(),
                        tenant.getPhoneNumber()
                ));

            } catch (Exception e) {
                emitError("Critical Verification Failure: " + e.getMessage());
            }
        }).start();
    }

    private void emitError(String message) {
        updateState(new UiState(false, message, false, "", 0.0, "", ""));
    }
}