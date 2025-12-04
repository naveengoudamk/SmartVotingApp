package com.example.smartvotingapp;

public class Election {
    private int id;
    private String title;
    private String state;
    private int minAge;
    private String status;
    private String stopDate;

    public Election(int id, String title, String state, int minAge, String status, String stopDate) {
        this.id = id;
        this.title = title;
        this.state = state;
        this.minAge = minAge;
        this.status = status;
        this.stopDate = stopDate;
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
}
