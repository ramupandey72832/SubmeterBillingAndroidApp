package com.github.devfrogora.service.impl;

import com.github.devfrogora.data.config.DatabaseConnection;
import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.service.DatabaseMigrationService;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.exception.ResourceNotFoundException;
import com.github.devfrogora.service.utils.OperationResult;

import java.sql.SQLException;

public class DatabaseMigratonServiceImpl  implements DatabaseMigrationService {
    @Override
    public OperationResult<Void> migrationDb() {
        try {
            DatabaseConnection.beginTransaction();

            boolean isDbMigrated = DaoManager.getDbMigrationDao().executeMigration();
            if(!isDbMigrated){
                throw new ResourceNotFoundException("Not Migrated.");
            }

            DatabaseConnection.commitTransaction();
            return OperationResult.success(null, "Hardware replaced. New meter sequence started.");
        } catch (Exception e) {
            DatabaseConnection.rollbackTransaction();
            return OperationResult.failure("Hardware swap failed: " + e.getMessage());
        }
    }



    @Override
    public void checkAndRunMigrations(MigrationCallback callback) {
        // Set the listener on the data layer briefly to catch the result
        DatabaseConnection.setMigrationListener(new DatabaseConnection.MigrationListener() {
            @Override
            public void onMigrationMessage(String message) {
                callback.onMessage(message);
            }

            @Override
            public void onMigrationError(String error, Exception e) {
                callback.onError(error, e);
            }
        });

        try {
            DatabaseConnection.migrationLogic();
        } catch (SQLException e) {
            callback.onError("Critical migration failure", e);
        }
    }
}
