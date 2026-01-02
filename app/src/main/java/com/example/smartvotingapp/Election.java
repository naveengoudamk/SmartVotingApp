package com.example.smartvotingapp;

public class Election {
    private int id;
    private String title;
    private String state;
    private int minAge;
    private String status;
    private String stopDate;
    private String resultDate;

    // Default constructor required for Firebase
    public Election() {
    }

    public Election(int id, String title, String state, int minAge, String status, String stopDate) {
        this(id, title, state, minAge, status, stopDate, null);
    }

    public Election(int id, String title, String state, int minAge, String status, String stopDate, String resultDate) {
        this.id = id;
        this.title = title;
        this.state = state;
        this.minAge = minAge;
        this.status = status;
        this.stopDate = stopDate;
        this.resultDate = resultDate;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getState() {
        return state;
    }

    public int getMinAge() {
        return minAge;
    }

    public String getStatus() {
        return status;
    }

    public String getStopDate() {
        return stopDate;
    }

    public String getResultDate() {
        return resultDate;
    }

    public void setResultDate(String resultDate) {
        this.resultDate = resultDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
