package com.example.smartvotingapp;

public class NotificationItem {
    public static final int TYPE_NEWS = 1;
    public static final int TYPE_ELECTION = 2;
    public static final int TYPE_FEEDBACK = 3;

    private int type;
    private String title;
    private String message;
    private long timestamp;
    private String referenceId; // ID to navigate to (e.g., news ID, election ID)

    public NotificationItem(int type, String title, String message, long timestamp, String referenceId) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.referenceId = referenceId;
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getReferenceId() {
        return referenceId;
    }
}
