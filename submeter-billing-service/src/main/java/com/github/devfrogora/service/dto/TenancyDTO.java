package com.github.devfrogora.service.dto;

public class TenancyDTO {

   private String roomNumber ;
    private  String tenantAaddhar;
    private String startDate;
    private String endDate;
    private int tenancyId;
    private int roomId;
    private int tenantId;

    public TenancyDTO(int tenancyId, int roomId, int tenantId,String roomNumber, String tenantAaddhar, String startDate,
                      String endDate ) {
        this.roomNumber = roomNumber;
        this.tenantAaddhar = tenantAaddhar;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tenancyId = tenancyId;
        this.roomId = roomId;
        this.tenantId = tenantId;
    }

    public String getTenantAaddhar() {
        return tenantAaddhar;
    }

    public void setTenantAaddhar(String tenantAaddhar) {
        this.tenantAaddhar = tenantAaddhar;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getTenancyId() {
        return tenancyId;
    }

    public void setTenancyId(int tenancyId) {
        this.tenancyId = tenancyId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}
