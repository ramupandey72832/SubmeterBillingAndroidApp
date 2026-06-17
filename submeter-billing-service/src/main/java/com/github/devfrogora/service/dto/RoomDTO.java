package com.github.devfrogora.service.dto;


public class RoomDTO {
    private String roomNumber;
    private String roomType; // e.g., "2BHK", "1RK"
    private double rentAmount;

    public RoomDTO(String roomNumber, double rentAmount, String roomType) {
        this.roomNumber = roomNumber;
        this.rentAmount = rentAmount;
        this.roomType = roomType;
    }

    // Getters and Setters
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public double getRentAmount() { return rentAmount; }
    public void setRentAmount(double rentAmount) { this.rentAmount = rentAmount; }
    public String getRoomType() { return roomType; }
}
