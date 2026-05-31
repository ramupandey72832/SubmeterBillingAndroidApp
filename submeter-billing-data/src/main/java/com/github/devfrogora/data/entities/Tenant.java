package com.github.devfrogora.data.entities;



public class Tenant {
    private int tenantId;
    private String name;
    private String phoneNumber;
    private String aadharNumber;
    private String address;
    private String email;
    private boolean isActive;

    public Tenant() {}

    public Tenant(int tenantId, String name, String phoneNumber, String aadharNumber, String address, String email, boolean isActive) {
        this.tenantId = tenantId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.aadharNumber = aadharNumber;
        this.address = address;
        this.email = email;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getTenantId() { return tenantId; }
    public void setTenantId(int tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "Tenant{" +
                "tenantId=" + tenantId +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", aadharNumber='" + aadharNumber + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}