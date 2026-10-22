package com.submeter.data.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    private int id;
    private int billId;
    private long amountPaise;
    private String paymentDate;
    private String note;
}
