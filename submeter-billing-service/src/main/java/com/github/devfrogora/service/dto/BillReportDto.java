package com.github.devfrogora.service.dto;

public class BillReportDto {
    private int billId;
    private String roomNumber;
    private String tenantName;
    private String billingDate;
    private double totalAmount;
    private String paymentStatus; // e.g., "PAID" or "UNPAID"

    // Constructor, Getters, and Setters
    public BillReportDto(int billId, String roomNumber, String tenantName, String billingDate, double totalAmount, String paymentStatus) {
        this.billId = billId;
        this.roomNumber = roomNumber;
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
}