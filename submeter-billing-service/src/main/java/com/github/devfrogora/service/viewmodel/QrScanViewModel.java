// File: submeter-billing-service/.../service/viewmodel/QrScanViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.RoomMeterService;

public class QrScanViewModel {

    private final RoomMeterService roomMeterService;

    // Simple primitive UI layout states
    private boolean isLoading = false;
    private String errorMessage = null;
    private String verifiedRoomNumber = null;

    public interface StateListener {
        void onStateChanged();
    }

    private StateListener listener;

    public QrScanViewModel(RoomMeterService roomMeterService) {
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
    public String getVerifiedRoomNumber() { return verifiedRoomNumber; }

    // --- UI Presentation Actions ---

    /**
     * Parses the scanned payload and checks the database for room validity.
     */
    public void processScannedData(String scannedPayload) {
        if (scannedPayload == null || scannedPayload.trim().isEmpty()) {
            this.errorMessage = "Scan Error: Empty barcode or QR code detected.";
            notifyUi();
            return;
        }

        // 1. Enforce string formatting filters
        if (!scannedPayload.contains("ROOM_NUMBER_")) {
            this.errorMessage = "Invalid Format: Scanned code is not a valid room QR identifier.";
            notifyUi();
            return;
        }

        // Reset states and clear prior successful entries
        this.isLoading = true;
        this.errorMessage = null;
        this.verifiedRoomNumber = null;
        notifyUi();

        // Extract raw target room number
        String roomNumber = scannedPayload.replace("ROOM_NUMBER_", "").trim();

        // 2. Perform validation and queries inside an isolated background thread
        new Thread(() -> {
            try {
                boolean isRoomExist = roomMeterService.isRoomExist(roomNumber);

                if (isRoomExist) {
                    this.verifiedRoomNumber = roomNumber;
                } else {
                    this.errorMessage = "Asset Failure: Room " + roomNumber + " does not exist in the configuration database.";
                }
            } catch (Exception e) {
                this.errorMessage = "Verification Error: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyUi();
            }
        }).start();
    }

    /**
     * Resets verified state tracking parameters to allow the camera to listen for a fresh scan hook.
     */
    public void resetVerificationState() {
        this.verifiedRoomNumber = null;
        this.errorMessage = null;
    }
}