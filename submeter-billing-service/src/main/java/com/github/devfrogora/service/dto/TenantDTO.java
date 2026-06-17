package com.github.devfrogora.service.dto;


/**
 * DTO to decouple Service Layer from Data Layer (Entities)
 */
public class TenantDTO {
    private String name;
    private String aadharNumber;
    private String phoneNumber;
    private String parentPhoneNumber;
    private String address;


    // Constructors
    public TenantDTO() {}

    public TenantDTO(String name, String aadharNumber, String phoneNumber,String address) {
        this.name = name;
        this.aadharNumber = aadharNumber;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getParentPhoneNumber() {
        return parentPhoneNumber;
    }

    public void setParentPhoneNumber(String parentPhoneNumber) {
        this.parentPhoneNumber = parentPhoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}