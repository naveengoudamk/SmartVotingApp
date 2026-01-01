package com.example.smartvotingapp;

public class VoteRecord {
    private String aadhaarId;
    private int electionId;
    private String optionId;
    private long timestamp;

    // Required for Firebase
    public VoteRecord() {
    }

    public VoteRecord(String aadhaarId, int electionId, String optionId, long timestamp) {
        this.aadhaarId = aadhaarId;
        this.electionId = electionId;
        this.optionId = optionId;
        this.timestamp = timestamp;
    }

    public String getAadhaarId() {
        return aadhaarId;
    }

    public void setAadhaarId(String aadhaarId) {
        this.aadhaarId = aadhaarId;
    }

    public int getElectionId() {
        return electionId;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
    }

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
