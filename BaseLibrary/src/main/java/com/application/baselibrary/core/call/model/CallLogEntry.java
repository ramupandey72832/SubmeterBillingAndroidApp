package com.application.baselibrary.core.call.model;

public class CallLogEntry {
    public enum CallType {
        INCOMING, OUTGOING, MISSED, REJECTED, VOICEMAIL, OTHER
    }

    public String number;
    public CallType type;
    public long date;
    public int duration;

    @Override
    public String toString() {
        return String.format("Number: %s, Type: %s, Date: %d, Duration: %d sec",
                number, type, date, duration);
    }

    public String uniqueKey() {
        return number + "_" + date + "_" + duration;
    }
}

