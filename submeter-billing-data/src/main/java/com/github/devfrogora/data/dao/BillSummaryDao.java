package com.github.devfrogora.data.dao;

import com.github.devfrogora.data.entities.BillSummary;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BillSummaryDao {
    Optional<BillSummary> getSummaryById(int billId) throws SQLException;
    List<BillSummary> getSummariesByRoom(String roomNumber) throws SQLException;
    List<BillSummary> getSummariesByTenantName(String tenantName) throws SQLException;
    List<BillSummary> getSummariesByStatus(boolean isPaid) throws SQLException;
    List<BillSummary> getAllSummaries() throws SQLException;
}