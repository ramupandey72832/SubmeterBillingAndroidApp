package com.github.devfrogora.data.dao.impl;

import com.github.devfrogora.data.config.SqlLoader;
import com.github.devfrogora.data.dao.SubmeterAllocationDao;
import com.github.devfrogora.data.dao.DbUtils;
import com.github.devfrogora.data.entities.SubmeterAllocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SQLiteSubmeterAllocationDao implements SubmeterAllocationDao {

    @Override
    public int insertAllocation(SubmeterAllocation allocation) throws SQLException {
        String sql = SqlLoader.get("submeter_allocation.insert");
        return DbUtils.executeInsert(sql,
                allocation.getMeterId(),
                allocation.getRoomId(),
                allocation.getStartDate(),
                allocation.getEndDate()
        );
    }

    @Override
    public Optional<SubmeterAllocation> getAllocationById(int allocationId) throws SQLException {
        String sql = SqlLoader.get("submeter_allocation.get_by_id");
        return DbUtils.executeQuerySingle(sql, this::mapResultSetToAllocation, allocationId);
    }

    @Override
    public List<SubmeterAllocation> getActiveAllocationsByMeter(int meterId) throws SQLException {
        String sql = SqlLoader.get("submeter_allocation.get_active_by_meter");
        return DbUtils.executeQueryList(sql, this::mapResultSetToAllocation, meterId);
    }

    @Override
    public List<SubmeterAllocation> getActiveAllocationsByRoom(int roomId) throws SQLException {
        String sql = SqlLoader.get("submeter_allocation.get_active_by_room");
        return DbUtils.executeQueryList(sql, this::mapResultSetToAllocation, roomId);
    }

    @Override
    public List<SubmeterAllocation> getAllAllocations() throws SQLException {
        String sql = SqlLoader.get("submeter_allocation.get_all");
        return DbUtils.executeQueryList(sql, this::mapResultSetToAllocation);
    }

    @Override
    public boolean endAllocation(int allocationId, String endDate) throws SQLException {
        String sql = SqlLoader.get("submeter_allocation.end");
        return DbUtils.executeUpdate(sql, endDate, allocationId);
    }

    @Override
    public boolean updateAllocation(SubmeterAllocation allocation) throws SQLException {
        String sql = SqlLoader.get("submeter_allocation.update");
        return DbUtils.executeUpdate(sql,
                allocation.getMeterId(),
                allocation.getRoomId(),
                allocation.getStartDate(),
                allocation.getEndDate(),
                allocation.getAllocationId()
        );
    }

    @Override
    public boolean deleteAllocation(int allocationId) throws SQLException {
        String sql = SqlLoader.get("submeter_allocation.delete");
        return DbUtils.executeUpdate(sql, allocationId);
    }

    /**
     * Maps a single row from the ResultSet to a SubmeterAllocation entity object.
     */
    private SubmeterAllocation mapResultSetToAllocation(ResultSet rs) throws SQLException {
        SubmeterAllocation allocation = new SubmeterAllocation();
        allocation.setAllocationId(rs.getInt("allocation_id"));
        allocation.setMeterId(rs.getInt("meter_id"));
        allocation.setRoomId(rs.getInt("room_id"));
        allocation.setStartDate(rs.getString("start_date"));
        allocation.setEndDate(rs.getString("end_date"));
        return allocation;
    }
}