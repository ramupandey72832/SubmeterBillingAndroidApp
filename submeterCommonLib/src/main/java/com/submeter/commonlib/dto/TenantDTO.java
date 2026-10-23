package com.submeter.commonlib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TenantDTO {
    private int id;
    private String name;
    private String phoneNumber;
    private String aadhaarNumber;
    private String address;

    public TenantDTO() {
        name = "";
        phoneNumber = "";
        aadhaarNumber = "";
        address = "";
    }
}