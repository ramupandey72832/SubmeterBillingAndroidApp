package com.github.devfrogora.service.dto.reports;

public class MeterReadingDTO {
    private int readingId;
    private int meterId;
    private double readingValue;
    private String imageUrlOrPath;
    private String readingDate; // Stores SQLite default TIMESTAMP strings

    public MeterReadingDTO() {}

    public MeterReadingDTO(int readingId, int meterId, double readingValue, String imageUrlOrPath, String readingDate) {
        this.readingId = readingId;
        this.meterId = meterId;
        this.readingValue = readingValue;
        this.imageUrlOrPath = imageUrlOrPath;
        this.readingDate = readingDate;
    }

    // Getters and Setters
    public int getReadingId() { return readingId; }
    public void setReadingId(int readingId) { this.readingId = readingId; }

    public int getMeterId() { return meterId; }
    public void setMeterId(int meterId) { this.meterId = meterId; }

    public double getReadingValue() { return readingValue; }
    public void setReadingValue(double readingValue) { this.readingValue = readingValue; }

    public String getImageUrlOrPath() { return imageUrlOrPath; }
    public void setImageUrlOrPath(String imageUrlOrPath) { this.imageUrlOrPath = imageUrlOrPath; }

    public String getReadingDate() { return readingDate; }
    public void setReadingDate(String readingDate) { this.readingDate = readingDate; }

    @Override
    public String toString() {
        return "MeterReading{" +
                "readingId=" + readingId +
                ", meterId=" + meterId +
                ", readingValue=" + readingValue +
                ", imageUrlOrPath='" + imageUrlOrPath + '\'' +
                ", readingDate='" + readingDate + '\'' +
                '}';
    }
}
