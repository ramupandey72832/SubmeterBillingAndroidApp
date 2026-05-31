package com.github.devfrogora.data.entities;

public class Bill {
    private int billId;
    private int previousReadingId;
    private int currentReadingId;
    private double unitsConsumed;
    private double ratePerUnit;
    private double totalAmount;
    private String billingDate; // Stored as ISO-8601 string (YYYY-MM-DD)
    private boolean isPaid;     // Maps cleanly to SQLite's 0/1 Integer

    // Default Constructor (Required for many serialization/mapping frameworks)
    public Bill() {
    }

    // Parameterized Constructor
    public Bill(int billId, int previousReadingId, int currentReadingId, double ratePerUnit, double totalAmount, String billingDate, boolean isPaid) {
        this.billId = billId;
        this.previousReadingId = previousReadingId;
        this.currentReadingId = currentReadingId;
        this.ratePerUnit = ratePerUnit;
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

    public int getPreviousReadingId() {
        return previousReadingId;
    }

    public void setPreviousReadingId(int previousReadingId) {
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
                ", ratePerUnit=" + ratePerUnit +
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
}