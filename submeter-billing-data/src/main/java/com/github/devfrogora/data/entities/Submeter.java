package com.github.devfrogora.data.entities;

import java.util.Objects;

public class Submeter {
    private int meterId;
    private int roomId;
    private String meterSerialNumber;
    private double initialReading;
    private int isActive;

    public Submeter() {}

    public Submeter(int meterId, int roomId, String meterSerialNumber, double initialReading,int isActive) {
        this.meterId = meterId;
        this.roomId = roomId;
        this.meterSerialNumber = meterSerialNumber;
        this.initialReading = initialReading;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getMeterId() { return meterId; }
    public void setMeterId(int meterId) { this.meterId = meterId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getMeterSerialNumber() { return meterSerialNumber; }
    public void setMeterSerialNumber(String meterSerialNumber) { this.meterSerialNumber = meterSerialNumber; }

    public double getInitialReading() { return initialReading; }
    public void setInitialReading(double initialReading) { this.initialReading = initialReading; }

    @Override
    public String toString() {
        return "Submeter{" +
                "meterId=" + meterId +
                ", roomId=" + roomId +
                ", meterSerialNumber='" + meterSerialNumber + '\'' +
                ", initialReading=" + initialReading +
                '}';
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }
}