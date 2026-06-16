// File: submeter-billing-service/.../service/viewmodel/QrScanViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.utils.CryptoHelper;

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
// Define your custom key here (Keep this exactly identical to the key used to generate the QR)
    private static final String MY_CUSTOM_KEY = "MySecretPassphrase123!";

    public void processScannedData(String scannedPayload) {
        if (scannedPayload == null || scannedPayload.trim().isEmpty()) {
            this.errorMessage = "Scan Error: Empty barcode or QR code detected.";
            notifyUi();
            return;
        }

        this.isLoading = true;
        this.errorMessage = null;
        this.verifiedRoomNumber = null;
        notifyUi();

        new Thread(() -> {
            try {
                // 1. Decode from Base64 & Decrypt using the Custom Key
                String decryptedPayload;
                try {
                    decryptedPayload = CryptoHelper.decryptFromBase64(scannedPayload, MY_CUSTOM_KEY);
                } catch (Exception e) {
                    this.errorMessage = "Security Error: Unable to decrypt QR code data. Invalid Key or Data.";
                    this.isLoading = false;
                    notifyUi();
                    return;
                }

                // 2. Validate the string formatting filters on the decrypted data
                if (!decryptedPayload.contains("ROOM_NUMBER_")) {
                    this.errorMessage = "Invalid Format: Scanned code is not a valid room QR identifier.";
                    this.isLoading = false;
                    notifyUi();
                    return;
                }

                // Extract target room number
                String roomNumber = decryptedPayload.replace("ROOM_NUMBER_", "").trim();

                // 3. Query the room service
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