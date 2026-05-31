package com.application.bottomnavigationbarui.dto;


public class RoomRegistryDto {
    private String roomNumber;
    private String tenantName;
    private String submeterSerialNumber;
    private boolean isVacant;

    // Constructor, Getters, and Setters
    public RoomRegistryDto(String roomNumber, String tenantName, String submeterSerialNumber, boolean isVacant) {
        this.roomNumber = roomNumber;
        this.tenantName = tenantName;
        this.submeterSerialNumber = submeterSerialNumber;
        this.isVacant = isVacant;
    }

    public String getRoomNumber() { return roomNumber; }
    public String getTenantName() { return tenantName; }
    public String getSubmeterSerialNumber() { return submeterSerialNumber; }
    public boolean isVacant() { return isVacant; }
}