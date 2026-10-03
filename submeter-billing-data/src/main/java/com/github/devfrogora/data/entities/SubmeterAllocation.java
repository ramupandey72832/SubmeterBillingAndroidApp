package com.github.devfrogora.data.entities;

import java.util.Objects;

public class SubmeterAllocation {
    private int allocationId;
    private int meterId;
    private int roomId;
    private String startDate;
    private String endDate;

    public SubmeterAllocation() {}

    public SubmeterAllocation(int allocationId, int meterId, int roomId, String startDate, String endDate) {
        this.allocationId = allocationId;
        this.meterId = meterId;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public int getAllocationId() { return allocationId; }
    public void setAllocationId(int allocationId) { this.allocationId = allocationId; }

    public int getMeterId() { return meterId; }
    public void setMeterId(int meterId) { this.meterId = meterId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    @Override
    public String toString() {
        return "SubmeterAllocation{" +
                "allocationId=" + allocationId +
                ", meterId=" + meterId +
                ", roomId=" + roomId +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubmeterAllocation that = (SubmeterAllocation) o;
        return allocationId == that.allocationId &&
                meterId == that.meterId &&
                roomId == that.roomId &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allocationId, meterId, roomId, startDate, endDate);
    }
}