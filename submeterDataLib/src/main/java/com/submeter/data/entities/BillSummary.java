package com.submeter.data.entities;

public class BillSummary {
    private int billId;
    private String roomNumber;
    private String tenantName;

    private long totalAmountPaise;
    private long totalPaidPaise;
    private long balancePaise;

    public BillSummary(
            int billId,
            String roomNumber,
            String tenantName,
            long totalAmountPaise,
            long totalPaidPaise) {

        this.billId = billId;
        this.roomNumber = roomNumber;
        this.tenantName = tenantName;
        this.totalAmountPaise = totalAmountPaise;
        this.totalPaidPaise = totalPaidPaise;

        this.balancePaise =
                totalAmountPaise - totalPaidPaise;
    }

    public long getBalancePaise() {
        return balancePaise;
    }

    public boolean hasOutstandingBalance() {
        return balancePaise > 0;
    }
}