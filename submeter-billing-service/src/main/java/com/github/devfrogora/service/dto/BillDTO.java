package com.github.devfrogora.service.dto;


public class BillDTO {
    private int billId;
    private String billingDate;
    private double unitsConsumed;
    private double ratePerUnit;
    private double totalAmount;
    private boolean isPaid;
    private String paymentDate;
    private double extraCharge;
    private String note;


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


    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public double getExtraCharge() { return extraCharge; }
    public void setExtraCharge(double extraCharge) { this.extraCharge = extraCharge; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}