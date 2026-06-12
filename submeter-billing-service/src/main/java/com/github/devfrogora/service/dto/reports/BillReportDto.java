package com.github.devfrogora.service.dto.reports;

public class BillReportDto {
    private int billId;
    private String roomNumber;
    private String meterSerialNumber;
    private double previousReading;
    private double currentReading;
    private double ratePerUnit;
    private double fixedCharge;
    private String tenantName;
    private String billingDate;
    private double totalAmount;
    private String paymentStatus; // e.g., "PAID" or "UNPAID"

    // Constructor, Getters, and Setters
    public BillReportDto(int billId, String roomNumber, String meterSerialNumber, double previousReading,
                         double currentReading, double ratePerUnit, double fixedCharge, String tenantName,
                         String billingDate, double totalAmount, String paymentStatus) {
        this.billId = billId;
        this.roomNumber = roomNumber;
        this.meterSerialNumber = meterSerialNumber;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
        this.ratePerUnit = ratePerUnit;
        this.fixedCharge = fixedCharge;
        this.tenantName = tenantName;
        this.billingDate = billingDate;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
    }

    public int getBillId() { return billId; }
    public String getRoomNumber() { return roomNumber; }
    public String getTenantName() { return tenantName; }
    public String getBillingDate() { return billingDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public void setMeterSerialNumber(String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    public double getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(double previousReading) {
        this.previousReading = previousReading;
    }

    public double getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(double currentReading) {
        this.currentReading = currentReading;
    }

    public double getRatePerUnit() {
        return ratePerUnit;
    }

    public void setRatePerUnit(double ratePerUnit) {
        this.ratePerUnit = ratePerUnit;
    }

    public double getFixedCharge() {
        return fixedCharge;
    }

    public void setFixedCharge(double fixedCharge) {
        this.fixedCharge = fixedCharge;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return "BillReportDto{" +
                "billId=" + billId +
                ", roomNumber='" + roomNumber + '\'' +
                ", tenantName='" + tenantName + '\'' +
                ", billingDate='" + billingDate + '\'' +
                ", meterSerialNumber='" + meterSerialNumber + '\'' +
                ", previousReading=" + previousReading +
                ", currentReading=" + currentReading +
                ", ratePerUnit=" + ratePerUnit +
                ", fixedCharge=" + fixedCharge +
                ", totalAmount=" + totalAmount +
                ", paymentStatus='" + paymentStatus + '\'' +
                '}';
    }
}