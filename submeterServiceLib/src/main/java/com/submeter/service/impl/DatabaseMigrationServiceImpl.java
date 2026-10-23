package com.submeter.service.impl;

import com.example.rrallutilitylib.db.DatabaseConnection;
import com.submeter.commonlib.response.OperationResult;
import com.submeter.commonlib.response.ResourceNotFoundException;
import com.submeter.data.dao.DaoManager;
import com.submeter.service.DatabaseMigrationService;

public class DatabaseMigrationServiceImpl
        implements DatabaseMigrationService {

    @Override
    public OperationResult<Void> migrationDb() {

        boolean transactionStarted = false;

        try {
            DatabaseConnection.beginTransaction();
            transactionStarted = true;

            boolean migrated =
                    DaoManager.getDbMigrationDao().executeMigration();

            if (!migrated) {
                throw new IllegalStateException(
                        "Database migration did not complete."
                );
            }

            DatabaseConnection.commitTransaction();
            transactionStarted = false;

            return OperationResult.success(
                    null,
                    "Database migration completed successfully."
            );

        } catch (Exception e) {

            if (transactionStarted) {
                try {
                    DatabaseConnection.rollbackTransaction();
                } catch (Exception rollbackException) {
                    e.addSuppressed(rollbackException);
                }
            }

            return OperationResult.failure(
                    "Database migration failed: " + e.getMessage()
            );
        }
    }
}