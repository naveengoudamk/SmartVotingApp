package com.example.smartvotingapp;

public class VoteRecord {
    private String aadhaarId;
    private int electionId;
    private String optionId;
    private long timestamp;

    public VoteRecord(String aadhaarId, int electionId, String optionId, long timestamp) {
        this.aadhaarId = aadhaarId;
        this.electionId = electionId;
        this.optionId = optionId;
        this.timestamp = timestamp;
    }

    public String getAadhaarId() {
        return aadhaarId;
    }

    public int getElectionId() {
        return electionId;
    }

    public String getOptionId() {
        return optionId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
