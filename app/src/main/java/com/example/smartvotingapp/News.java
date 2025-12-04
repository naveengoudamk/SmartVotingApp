package com.example.smartvotingapp;

public class News {
    private String id;
    private String title;
    private String description;
    private String date;
    private long timestamp;
    private String imageUrl;

    public News(String id, String title, String description, String date, long timestamp, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
