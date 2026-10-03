package com.github.devfrogora.data.dao;

import com.github.devfrogora.data.entities.SubmeterAllocation;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SubmeterAllocationDao {
    int insertAllocation(SubmeterAllocation allocation) throws SQLException;
    Optional<SubmeterAllocation> getAllocationById(int allocationId) throws SQLException;
    List<SubmeterAllocation> getActiveAllocationsByMeter(int meterId) throws SQLException;
    List<SubmeterAllocation> getActiveAllocationsByRoom(int roomId) throws SQLException;
    List<SubmeterAllocation> getAllAllocations() throws SQLException;
    boolean endAllocation(int allocationId, String endDate) throws SQLException;
    boolean updateAllocation(SubmeterAllocation allocation) throws SQLException;
    boolean deleteAllocation(int allocationId) throws SQLException;
}