package com.github.devfrogora.service;

import com.github.devfrogora.service.utils.OperationResult;

public interface DatabaseMigrationService {
    OperationResult<Void> migrationDb();
    void checkAndRunMigrations(MigrationCallback callback);// New interface for decoupling messages
    interface MigrationCallback {
        void onMessage(String msg);
        void onError(String err, Exception e);
    }
}
