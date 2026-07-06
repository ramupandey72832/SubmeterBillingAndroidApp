// File: submeter-billing-service/.../service/viewmodel/RoomMeterViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.utils.OperationResult;

import java.util.ArrayList;
import java.util.List;

public class RoomMeterViewModel {

    private final RoomMeterService service;

    // Direct, simple state fields
    private boolean isLoading = false;
    private String errorMessage = null;
    private boolean isOperationSuccess = false;
    private List<RoomRegistryDto> roomReportsList = new ArrayList<>();
    private List<String> roomNumbersList = new ArrayList<>();

    // A simple, classic interface listener to notify the UI when fields change
    public interface StateListener {
        void onStateChanged();
    }

    private StateListener listener;

    public RoomMeterViewModel(RoomMeterService service) {
        this.service = service;
    }

    public void setStateListener(StateListener listener) {
        this.listener = listener;
    }

    private void notifyUi() {
        if (listener != null) {
            listener.onStateChanged();
        }
    }

    // Standard Getters for your Fragment to pull values from
    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isOperationSuccess() { return isOperationSuccess; }
    public List<RoomRegistryDto> getRoomReportsList() { return roomReportsList; }
    public List<String> getRoomNumbersList() { return roomNumbersList; }

    // --- UI Actions ---

    public void createRoom(String num, String type, String serial, double reading) {
        // Reset states before starting the work
        this.isLoading = true;
        this.errorMessage = null;
        this.isOperationSuccess = false;
        notifyUi();

        // Run on a background thread so the Android screen doesn't freeze
        new Thread(() -> {
            // Receive the result package from our professional Service Layer
            OperationResult<String> result = service.addRoomWithMeter(num, type, serial, reading);

            // Directly modify the simple variables based on database outcome
            this.isLoading = false;
            this.isOperationSuccess = result.isSuccess();

            if (!result.isSuccess()) {
                // Database or Business rules custom exception string captured here!
                this.errorMessage = result.getMessage();
            }

            // Tell the UI thread to re-render the screen
            notifyUi();
        }).start();
    }

    // --- New Room Deletion Action ---
    public void deleteRoom(String roomNumber) {
        this.isLoading = true;
        this.errorMessage = null;
        this.isOperationSuccess = false;
        notifyUi();

        new Thread(() -> {
            // Invokes the service layer which returns a structured OperationResult
            OperationResult<Void> result = service.deleteRoomIfVacant(roomNumber);

            this.isLoading = false;
            this.isOperationSuccess = result.isSuccess();

            if (!result.isSuccess()) {
                this.errorMessage = result.getMessage(); // Captures your custom domain errors
            }

            notifyUi();
        }).start();
    }

    // --- New Submeter Replacement Action ---
    public void replaceSubmeter(String roomNumber, String oldSerialNumber, String newSerialNumber,double initialReading) {
        this.isLoading = true;
        this.errorMessage = null;
        this.isOperationSuccess = false;
        notifyUi();

        new Thread(() -> {
            // Service method now cleanly responds with an OperationResult package
            OperationResult<Void> result = service.updateSubmeter(roomNumber, oldSerialNumber, newSerialNumber,initialReading);

            this.isLoading = false;
            this.isOperationSuccess = result.isSuccess();

            if (!result.isSuccess()) {
                this.errorMessage = result.getMessage(); // Captures database/resource failures safely
            }

            notifyUi();
        }).start();
    }

    // --- New Room Registry List Loader ---
    public void loadRoomReports() {
        this.isLoading = true;
        this.errorMessage = null;
        notifyUi();

        new Thread(() ->  {
            OperationResult<List<RoomRegistryDto>> result = service.getAllRoomReport();

            if (result.isSuccess()) {
                // Post payload cleanly to the main UI thread recycler data observers
                this.roomReportsList = result.getData();
                this.isOperationSuccess = true;
            } else {
                // Post error messages to a single-use action live event to display a Toast or Snackbar
                this.errorMessage = "Failed to compile room inventory matrix: " + result.getMessage();
            }
            this.isLoading = false;
            notifyUi();
        }).start();
    }

    public void loadAllRoomNumbers() {
        this.isLoading = true;
        this.errorMessage = null;
        this.isOperationSuccess = false;
        notifyUi();

        new Thread(() -> {
            // Invokes the service layer to get the bare list of room identifiers
            OperationResult<List<String>> result = service.getAllRoomNumbers();

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
}