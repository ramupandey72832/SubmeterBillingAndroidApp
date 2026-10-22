package com.submeter.service;

import com.submeter.commonlib.response.OperationResult;

public interface DatabaseMigrationService {
    OperationResult<Void> migrationDb();
}
