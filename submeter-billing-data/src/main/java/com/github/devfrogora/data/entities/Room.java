package com.github.devfrogora.data.entities;


public class Room {
    private int roomId;
    private String roomNumber;
    private double rentAmount;
    private String roomType;

    public Room() {}

    public Room(int roomId, String roomNumber, double rentAmount,String roomType) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.rentAmount = rentAmount;
        this.roomType = roomType;
    }

    // Getters and Setters
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public double getRentAmount() { return rentAmount; }
    public void setRentAmount(double rentAmount) { this.rentAmount = rentAmount; }

    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", roomNumber='" + roomNumber + '\'' +
                ", roomType='" + roomType + '\'' +
                ", rentAmount=" + rentAmount +
                '}';
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }
}