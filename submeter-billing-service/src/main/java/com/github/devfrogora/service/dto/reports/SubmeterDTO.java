package com.github.devfrogora.service.dto.reports;

public class SubmeterDTO {

    private int meterId;
    private int roomId;
    private String meterSerialNumber;
    private double initialReading;

    public SubmeterDTO() {}

    public SubmeterDTO(int meterId, int roomId, String meterSerialNumber, double initialReading) {
        this.meterId = meterId;
        this.roomId = roomId;
        this.meterSerialNumber = meterSerialNumber;
        this.initialReading = initialReading;
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
}