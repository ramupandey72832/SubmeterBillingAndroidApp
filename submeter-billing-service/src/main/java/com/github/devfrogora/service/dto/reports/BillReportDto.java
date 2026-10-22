package com.github.devfrogora.service.dto.reports;

import java.io.Serializable;

// FIX: Append "implements Serializable" to the class header declaration


public class BillReportDto implements Serializable {

    // It is highly recommended to declare a stable version ID for safe serialization
    private static final long serialVersionUID = 1L;
    private int billId;
    private String roomNumber;
    private String meterSerialNumber;
    private long previousReading;
    private double currentReading;
    private double ratePerUnit;
    private double fixedCharge;
    private String tenantName;
    private String billingDate;
    private double totalAmount;
    private String paymentDate;
    private double extraCharge;
    private String note;

    // Constructor, Getters, and Setters

    public BillReportDto(int billId, String roomNumber, String meterSerialNumber, long previousReading,
                         long currentReading, double ratePerUnit, double fixedCharge,double extraCharge, String tenantName,
                         String note,double totalAmount,String billingDate) {
        this.billId = billId;
        this.roomNumber = roomNumber;
        this.meterSerialNumber = meterSerialNumber;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
        this.ratePerUnit = ratePerUnit;
        this.fixedCharge = fixedCharge;
        this.extraCharge = extraCharge;
        this.tenantName = tenantName;
        this.note = note;
        this.totalAmount = totalAmount;
        this.billingDate = billingDate;
        this.paymentDate = paymentDate;
    }

    public int getBillId() { return billId; }
    public String getRoomNumber() { return roomNumber; }
    public String getTenantName() { return tenantName; }
    public String getBillingDate() { return billingDate; }
    public double getTotalAmount() { return totalAmount; }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public void setMeterSerialNumber(String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    public long getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(long previousReading) {
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




    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public double getExtraCharge() { return extraCharge; }
    public void setExtraCharge(double extraCharge) { this.extraCharge = extraCharge; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

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
                ", extraCharge=" + extraCharge +
                ", totalAmount=" + totalAmount +
                ", note=" + note +
                ", paymentDate=" + paymentDate +
                '}';
    }
}