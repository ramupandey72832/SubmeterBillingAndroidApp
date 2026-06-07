package com.github.devfrogora.data.entities;

public class Bill {
    private int billId;
    private Integer previousReadingId;
    private int currentReadingId;
    private int meterId;
    private String meterSerialNumber;
    private Integer tenantId;
    private String tenantName;
    private double unitsConsumed;
    private double ratePerUnit;
    private String roomNumber;
    private double fixedCharge;
    private double totalAmount;
    private String billingDate; // Stored as ISO-8601 string (YYYY-MM-DD)
    private boolean isPaid;     // Maps cleanly to SQLite's 0/1 Integer

    // Default Constructor (Required for many serialization/mapping frameworks)
    public Bill() {
    }

    // Parameterized Constructor
    public Bill(int billId, Integer previousReadingId , int currentReadingId, int meterId, String meterSerialNumber,
                Integer tenantId, String tenantName, double unitsConsumed, double ratePerUnit, String roomNumber,
                double fixedCharge, double totalAmount, String billingDate, boolean isPaid) {
        this.billId = billId;
        this.previousReadingId = previousReadingId;
        this.currentReadingId = currentReadingId;
        this.meterId = meterId;
        this.meterSerialNumber = meterSerialNumber;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.unitsConsumed = unitsConsumed;
        this.ratePerUnit = ratePerUnit;
        this.roomNumber = roomNumber;
        this.fixedCharge = fixedCharge;
        this.totalAmount = totalAmount;
        this.billingDate = billingDate;
        this.isPaid = isPaid;
    }

    // Getters and Setters
    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public Integer getPreviousReadingId() {
        return previousReadingId;
    }

    public void setPreviousReadingId(Integer previousReadingId) {
        this.previousReadingId = previousReadingId;
    }

    public int getCurrentReadingId() {
        return currentReadingId;
    }

    public void setCurrentReadingId(int currentReadingId) {
        this.currentReadingId = currentReadingId;
    }

    public double getRatePerUnit() {
        return ratePerUnit;
    }

    public void setRatePerUnit(double ratePerUnit) {
        this.ratePerUnit = ratePerUnit;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(String billingDate) {
        this.billingDate = billingDate;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    // toString() implementation for logging and debugging
    @Override
    public String toString() {
        return "Bill{" +
                "billId=" + billId +
                ", previousReadingId=" + previousReadingId +
                ", currentReadingId=" + currentReadingId +
                ", meterId=" + meterId +
                ", meterSerialNumber='" + meterSerialNumber + '\'' +
                ", tenantId=" + tenantId +
                ", tenantName='" + tenantName + '\'' +
                ", unitsConsumed=" + unitsConsumed +
                ", ratePerUnit=" + ratePerUnit +
                ", roomNumber='" + roomNumber + '\'' +
                ", fixedCharge=" + fixedCharge +
                ", totalAmount=" + totalAmount +
                ", billingDate='" + billingDate + '\'' +
                ", isPaid=" + isPaid +
                '}';
    }

    public double getUnitsConsumed() {
        return unitsConsumed;
    }

    public void setUnitsConsumed(double unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public void setMeterSerialNumber(String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    public int getMeterId() {
        return meterId;
    }

    public void setMeterId(int meterId) {
        this.meterId = meterId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public double getFixedCharge() {
        return fixedCharge;
    }

    public void setFixedCharge(double fixedCharge) {
        this.fixedCharge = fixedCharge;
    }
}