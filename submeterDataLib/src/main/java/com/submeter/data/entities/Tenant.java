package com.submeter.data.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
public class Tenant {
    private int id;
    private String name;
    private String phoneNumber;
    private String aadhaarNumber;
    private String address;

    public Tenant() {
        name = "";
        phoneNumber = "";
        aadhaarNumber = "";
        address = "";
    }
}