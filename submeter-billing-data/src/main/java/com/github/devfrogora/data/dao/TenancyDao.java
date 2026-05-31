package com.github.devfrogora.data.dao;

import com.github.devfrogora.data.entities.Tenancy;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TenancyDao {
    boolean insertTenancy(Tenancy tenancy) throws SQLException;
    Optional<Tenancy> getActiveTenancyByRoomId(int roomId) throws SQLException;

    List<Tenancy> getTenancyHistoryByRoomId(int roomId) throws SQLException;
    boolean endContract(int roomId, int tenantId, String endDate) throws SQLException;
    List<Tenancy> getAllTenancies() throws SQLException;
}