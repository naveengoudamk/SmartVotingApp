package com.example.smartvotingapp;

public class Feedback {
    private String id;
    private String userId;
    private String userName;
    private String userAadhaar;
    private String userState; // Added for state-based filtering
    private String title;
    private String description;
    private String status; // "pending", "in_progress", "resolved"
    private String adminResponse;
    private long timestamp;
    private long resolvedTimestamp;

    public Feedback() {
    }

    public Feedback(String id, String userId, String userName, String userAadhaar,
            String userState, String title, String description, String status,
            String adminResponse, long timestamp, long resolvedTimestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userAadhaar = userAadhaar;
        this.userState = userState;
        this.title = title;
        this.description = description;
        this.status = status;
        this.adminResponse = adminResponse;
        this.timestamp = timestamp;
        this.resolvedTimestamp = resolvedTimestamp;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserAadhaar() {
        return userAadhaar;
    }

    public String getUserState() {
        return userState;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getResolvedTimestamp() {
        return resolvedTimestamp;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserAadhaar(String userAadhaar) {
        this.userAadhaar = userAadhaar;
    }

    public void setUserState(String userState) {
        this.userState = userState;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setResolvedTimestamp(long resolvedTimestamp) {
        this.resolvedTimestamp = resolvedTimestamp;
    }
}
