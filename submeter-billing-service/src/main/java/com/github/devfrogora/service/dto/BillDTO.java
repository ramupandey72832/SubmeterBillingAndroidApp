package com.github.devfrogora.service.dto;


public class BillDTO {
    private int billId;
    private String billingDate;
    private double unitsConsumed;
    private double ratePerUnit;
    private double totalAmount;
    private boolean isPaid;

    public BillDTO(int billId, String billingDate, double unitsConsumed, double ratePerUnit, double totalAmount, boolean isPaid) {
        this.billId = billId;
        this.billingDate = billingDate;
        this.unitsConsumed = unitsConsumed;
        this.ratePerUnit = ratePerUnit;
        this.totalAmount = totalAmount;
        this.isPaid = isPaid;
    }

    // Getters
    public int getBillId() { return billId; }
    public String getBillingDate() { return billingDate; }
    public double getUnitsConsumed() { return unitsConsumed; }
    public double getTotalAmount() { return totalAmount; }
    public boolean isPaid() { return isPaid; }
}