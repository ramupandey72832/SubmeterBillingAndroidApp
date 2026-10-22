package com.submeter.presentation.viewmodel;

import com.submeter.commonlib.response.OperationResult;
import com.submeter.service.DatabaseMigrationService;

public class DatabaseMigrationViewModel extends BaseViewModel {

    private final DatabaseMigrationService service;
    private String migrationStatus; // remove this and its getter and setter , it have no use case

    public DatabaseMigrationViewModel(DatabaseMigrationService service) {
        this.service = service;
    }

    public void runMigrationDb() {
        setLoading(true);
        setErrorMessage(null);
        setOperationSuccess(false);

        new Thread(() -> {
            OperationResult<Void> result = service.migrationDb();
            migrationStatus = result.getMessage();

            setOperationSuccess(result.isSuccess());

            if (!result.isSuccess()) {
                setErrorMessage(result.getMessage());
            }

            setLoading(false);
        }).start();
    }

    public String getMigrationStatus() {
        return migrationStatus;
    }

    public void clearMigrationStatus() {
        migrationStatus = null;
        notifyStateChanged();
    }
}
