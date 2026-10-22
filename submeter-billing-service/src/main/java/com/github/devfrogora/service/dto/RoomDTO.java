package com.github.devfrogora.service.dto;


public class RoomDTO {
    private String roomNumber;
    private String roomType; // e.g., "2BHK", "1RK"

    public RoomDTO(String roomNumber, String roomType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
    }

    // Getters and Setters
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomType() { return roomType; }
}
