package com.application.baselibrary.notification.model;

public class NotificationData {
    private String packageName;
    private String title;
    private String text;
    private long timestamp;
    private String category;

    public NotificationData(String packageName, String title, String text, long timestamp, String category) {
        this.packageName = packageName;
        this.title = title;
        this.text = text;
        this.timestamp = timestamp;
        this.category = category;
    }

    // Getters
    public String getPackageName() { return packageName; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return "App: " + packageName +
                "\nCategory: " + category +
                "\nTitle: " + title +
                "\nText: " + text +
                "\nTime: " + timestamp;
    }
}
