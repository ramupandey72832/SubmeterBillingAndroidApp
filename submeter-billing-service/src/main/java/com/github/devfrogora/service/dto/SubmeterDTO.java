package com.github.devfrogora.service.dto;

public class SubmeterDTO {

    private int meterId;
    private String meterSerialNumber;
    private double initialReading;

    public SubmeterDTO() {}

    public SubmeterDTO(int meterId, String meterSerialNumber, double initialReading) {
        this.meterId = meterId;
        this.meterSerialNumber = meterSerialNumber;
        this.initialReading = initialReading;
    }

    // Getters and Setters
    public int getMeterId() { return meterId; }
    public void setMeterId(int meterId) { this.meterId = meterId; }


    public String getMeterSerialNumber() { return meterSerialNumber; }
    public void setMeterSerialNumber(String meterSerialNumber) { this.meterSerialNumber = meterSerialNumber; }

    public double getInitialReading() { return initialReading; }
    public void setInitialReading(double initialReading) { this.initialReading = initialReading; }

    @Override
    public String toString() {
        return "Submeter{" +
                "meterId=" + meterId +
                ", meterSerialNumber='" + meterSerialNumber + '\'' +
                ", initialReading=" + initialReading +
                '}';
    }
}