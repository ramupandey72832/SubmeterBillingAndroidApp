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

        try {
            DatabaseConnection.beginTransaction();

            boolean isDbMigrated =
                    DaoManager.getDbMigrationDao().executeMigration();

            if (!isDbMigrated) {
                throw new ResourceNotFoundException("Not Migrated.");
            }

            DatabaseConnection.commitTransaction();

            return OperationResult.success(
                    null,
                    "DB migration successfully."
            );

        } catch (Exception e) {

            try {
                DatabaseConnection.rollbackTransaction();
            } catch (Exception rollbackException) {
                e.addSuppressed(rollbackException);
            }

            return OperationResult.failure(
                    "Database migration failed: " + e.getMessage()
            );
        }
    }
}