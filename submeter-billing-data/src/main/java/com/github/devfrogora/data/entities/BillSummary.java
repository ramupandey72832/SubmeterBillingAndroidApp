package com.github.devfrogora.data.entities;


public class BillSummary {
    private int billId;
    private String roomNumber;
    private String tenantName;
    private String meterSerialNumber;
    private double previousReading;
    private double currentReading;
    private double unitsConsumed;
    private double ratePerUnit;
    private double totalAmount;
    private String billingDate;
    private boolean isPaid;

    // Default Constructor
    public BillSummary() {}

    // Parameterized Constructor
    public BillSummary(int billId, String roomNumber, String tenantName, String meterSerialNumber,
                       double previousReading, double currentReading, double unitsConsumed,
                       double ratePerUnit, double totalAmount, String billingDate, boolean isPaid) {
        this.billId = billId;
        this.roomNumber = roomNumber;
        this.tenantName = tenantName;
        this.meterSerialNumber = meterSerialNumber;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
        this.unitsConsumed = unitsConsumed;
        this.ratePerUnit = ratePerUnit;
        this.totalAmount = totalAmount;
        this.billingDate = billingDate;
        this.isPaid = isPaid;
    }

    // Getters and Setters
    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getMeterSerialNumber() { return meterSerialNumber; }
    public void setMeterSerialNumber(String meterSerialNumber) { this.meterSerialNumber = meterSerialNumber; }

    public double getPreviousReading() { return previousReading; }
    public void setPreviousReading(double previousReading) { this.previousReading = previousReading; }

    public double getCurrentReading() { return currentReading; }
    public void setCurrentReading(double currentReading) { this.currentReading = currentReading; }

    public double getUnitsConsumed() { return unitsConsumed; }
    public void setUnitsConsumed(double unitsConsumed) { this.unitsConsumed = unitsConsumed; }

    public double getRatePerUnit() { return ratePerUnit; }
    public void setRatePerUnit(double ratePerUnit) { this.ratePerUnit = ratePerUnit; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getBillingDate() { return billingDate; }
    public void setBillingDate(String billingDate) { this.billingDate = billingDate; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    @Override
    public String toString() {
        return "BillSummary{" +
                "billId=" + billId +
                ", roomNumber='" + roomNumber + '\'' +
                ", tenantName='" + tenantName + '\'' +
                ", meterSerialNumber='" + meterSerialNumber + '\'' +
                ", previousReading=" + previousReading +
                ", currentReading=" + currentReading +
                ", unitsConsumed=" + unitsConsumed +
                ", ratePerUnit=" + ratePerUnit +
                ", totalAmount=" + totalAmount +
                ", billingDate='" + billingDate + '\'' +
                ", isPaid=" + isPaid +
                '}';
    }
}