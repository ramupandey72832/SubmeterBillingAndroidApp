package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.service.DatabaseMigrationService;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.utils.OperationResult;

public class DatabaseMigrationViewModel {
    private boolean isLoading = false;
    private String errorMessage = null;
    private boolean isOperationSuccess = false;

    DatabaseMigrationService service;

    public DatabaseMigrationViewModel(DatabaseMigrationService service) {
        this.service = service;
    }

    // A simple, classic interface listener to notify the UI when fields change
    public interface StateListener {
        void onStateChanged();
    }

    private RoomMeterViewModel.StateListener listener;

    public void setStateListener(RoomMeterViewModel.StateListener listener) {
        this.listener = listener;
    }

    private void notifyUi() {
        if (listener != null) {
            listener.onStateChanged();
        }
    }

    public void runMigrationDb(){
        this.isLoading = true;
        this.errorMessage = null;
        this.isOperationSuccess = false;
        notifyUi();
        new Thread(() -> {
            // Service method now cleanly responds with an OperationResult package
            OperationResult<Void> result = service.migrationDb();

            this.isLoading = false;
            this.isOperationSuccess = result.isSuccess();

            if (!result.isSuccess()) {
                this.errorMessage = result.getMessage(); // Captures database/resource failures safely
            }

            notifyUi();

        }).start();
    }

    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isOperationSuccess() { return isOperationSuccess; }
    String migrationStatus;
    public void performDatabaseCheck() {
        service.checkAndRunMigrations(new DatabaseMigrationService.MigrationCallback() {
            @Override
            public void onMessage(String msg) {
                migrationStatus = msg;
                notifyUi(); // Trigger UI update
            }

            @Override
            public void onError(String err, Exception e) {
                errorMessage = err + ": " + e.getMessage();
                notifyUi();
            }
        });
    }

    public String getMigrationStatus() { return migrationStatus; }
    public void clearMigrationStatus() {
        this.migrationStatus = null;
    }

}
