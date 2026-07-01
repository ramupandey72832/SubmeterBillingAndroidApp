// File: submeter-billing-service/.../service/viewmodel/TenantViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.dto.TenancyDTO;
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.utils.OperationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TenantViewModel {

    private final TenancyManagementService tenancyManagementService;
    private final RoomMeterService roomMeterService;

    // Direct, primitive view status states
    private boolean isLoading = false;
    private String errorMessage = null;
    private boolean isOperationSuccess = false;
    private String feedbackMessage = null;
    private List<String> roomNumbersList = new ArrayList<>();



    public TenantViewModel(TenancyManagementService tenancyManagementService, RoomMeterService roomMeterService) {
        this.tenancyManagementService = tenancyManagementService;
        this.roomMeterService = roomMeterService;
    }

    // Interface callback to update the observer layout
    public interface StateListener {
        void onStateChanged();
    }

    private StateListener listener;


    public void setStateListener(StateListener listener) {
        this.listener = listener;
    }

    private void notifyUi() {
        if (listener != null) {
            listener.onStateChanged();
        }
    }

    // Standard presentation layer getters
    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isOperationSuccess() { return isOperationSuccess; }
    public String getFeedbackMessage() { return feedbackMessage; }
    /**
     * MODIFIED GETTER: Automatically guarantees the data layer is read
     * and hydrated before returning the list block back to the View.
     */
    public List<String> getRoomNumbersList() {
        if (roomNumbersList == null || roomNumbersList.isEmpty()) {
            loadAllRoomNumbersSynchronously();
        }
        return roomNumbersList;
    }


    // --- UI Presentation Actions ---

    public void onboardNewTenant(String name, String governmentId, String mobile, String parentMobile, String address, String roomNum, String startDt) {
        // 1. Initial Local Business Checks
        if (name == null || name.trim().isEmpty() ||
                governmentId == null || governmentId.trim().isEmpty() ||
                mobile == null || mobile.trim().isEmpty() ||
                roomNum == null || roomNum.trim().isEmpty() ||
                startDt == null || startDt.trim().isEmpty()) {

            this.errorMessage = "Validation Error: Mandate inputs cannot be left blank.";
            notifyUi();
            return;
        }

        // Reset tracking states prior to running worker loops
        this.isLoading = true;
        this.errorMessage = null;
        this.isOperationSuccess = false;
        notifyUi();

        // 2. Perform backend assignments on an isolated execution thread
        new Thread(() -> {
            try {
                TenantDTO tenantDTO = new TenantDTO();
                tenantDTO.setName(name.trim());
                tenantDTO.setAadharNumber(governmentId.trim());
                tenantDTO.setPhoneNumber(mobile.trim());
                tenantDTO.setParentPhoneNumber(parentMobile != null ? parentMobile.trim() : "");
                tenantDTO.setAddress(address != null ? address.trim() : "");

                // Execute the service routine business transactions
                tenancyManagementService.addTenantWithTenancy(tenantDTO, roomNum.trim(), startDt.trim());

                this.isOperationSuccess = true;
            } catch (Exception e) {
                // Captures custom business validation or transaction exceptions safely
                this.errorMessage = e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    // --- Tenant Deletion Action with Embedded Safety Logic ---
    public void deleteTenant(String aadhaarNumber, String roomNumber) {
        if (aadhaarNumber == null || aadhaarNumber.trim().isEmpty() || roomNumber == null || roomNumber.trim().isEmpty()) {
            this.errorMessage = "Validation Error: Aadhaar and Room Number are mandatory fields.";
            notifyUi();
            return;
        }

        this.isLoading = true;
        this.errorMessage = null;
        this.isOperationSuccess = false;
        this.feedbackMessage = null;
        notifyUi();

        new Thread(() -> {
            try {
                String cleanRoom = roomNumber.trim();
                String cleanAadhaar = aadhaarNumber.trim();

                // 1. Verify structural room existence
                boolean isRoomExist = roomMeterService.isRoomExist(cleanRoom);
                if (!isRoomExist) {
                    this.errorMessage = "Business Validation: Room " + cleanRoom + " does not exist in the system.";
                    this.isLoading = false;
                    notifyUi();
                    return;
                }

                // 2. Query active tenancy contracts
                TenancyDTO tenancyDTO = tenancyManagementService.findActiveTenancyByTenantAadhar(cleanAadhaar);

                if (tenancyDTO != null) {
                    // Tenant has an active tenancy layout somewhere
                    if (tenancyDTO.getRoomNumber().equalsIgnoreCase(cleanRoom)) {
                        this.errorMessage = "Safety Restriction: Tenant cannot be hard-deleted while actively occupying room " + cleanRoom + ". Terminate their tenancy structure first.";
                    } else {
                        this.errorMessage = "Safety Restriction: Tenant has an active tenancy contract tied to room: " + tenancyDTO.getRoomNumber();
                    }
                    this.isLoading = false;
                    notifyUi();
                    return;
                }

                // 3. Safe Deletion execution path (No active tenancy blocking history constraints)
                tenancyManagementService.deleteTenantIfNoActiveTenancy(cleanAadhaar);
                this.feedbackMessage = "Tenant record successfully dropped from systems.";
                this.isOperationSuccess = true;

            } catch (Exception e) {
                this.errorMessage = "Critical database failure during tenant removal: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    public void loadAllRoomNumbers() {
        this.isLoading = true;
        this.errorMessage = null;
        this.isOperationSuccess = false;
        notifyUi();

        new Thread(() -> {
            // Invokes the service layer to get the bare list of room identifiers
            OperationResult<List<String>> result = roomMeterService.getAllRoomNumbers();

            if (result.isSuccess()) {
                this.roomNumbersList = result.getData();
                this.isOperationSuccess = true;
            } else {
                this.errorMessage = "Failed to load room numbers: " + result.getMessage();
            }
            this.isLoading = false;
            notifyUi();
        }).start();
    }

    private void loadAllRoomNumbersSynchronously() {
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                OperationResult<List<String>> result = roomMeterService.getAllRoomNumbers();
                if (result.isSuccess()) {
                    this.roomNumbersList = result.getData();
                    this.isOperationSuccess = true;
                } else {
                    this.errorMessage = result.getMessage();
                }
            } finally {
                latch.countDown();
            }
        }).start();

        try {
            // FIXED: Only blocks for a maximum of 1000 milliseconds.
            // If the DB takes longer, it breaks out safely instead of freezing the app.
            boolean success = latch.await(6000, java.util.concurrent.TimeUnit.MILLISECONDS);

            if (!success) {
                this.errorMessage = "Timeout Error: Database took too long to return room records.";
                // Fill with an empty list so the app doesn't crash on null loops
                if (this.roomNumbersList == null) {
                    this.roomNumbersList = new java.util.ArrayList<>();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.errorMessage = "Room loading synchronization interrupted: " + e.getMessage();
        }
    }
}