package com.github.devfrogora.data.entities;


import java.util.Objects;

public class Tenancy {
    private int tenancyId;
    private int roomId;
    private int tenantId;
    private String startDate; // ISO-8601 string (YYYY-MM-DD)
    private String endDate;   // ISO-8601 string (YYYY-MM-DD), NULL if active

    public Tenancy() {}

    public Tenancy(int tenancyId, int roomId, int tenantId, String startDate, String endDate) {
        this.tenancyId = tenancyId;
        this.roomId = roomId;
        this.tenantId = tenantId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public int getTenancyId() { return tenancyId; }
    public void setTenancyId(int tenancyId) { this.tenancyId = tenancyId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public int getTenantId() { return tenantId; }
    public void setTenantId(int tenantId) { this.tenantId = tenantId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    @Override
    public String toString() {
        return "Tenancy{" +
                "tenancyId=" + tenancyId +
                ", roomId=" + roomId +
                ", tenantId=" + tenantId +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}