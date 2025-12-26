package com.example.smartvotingapp;

public class VotingOption {
    private String id;
    private int electionId;
    private String optionName;
    private String description;
    private String logoPath;

    public VotingOption(String id, int electionId, String optionName, String description) {
        this(id, electionId, optionName, description, null);
    }

    public VotingOption(String id, int electionId, String optionName, String description, String logoPath) {
        this.id = id;
        this.electionId = electionId;
        this.optionName = optionName;
        this.description = description;
        this.logoPath = logoPath;
    }

    public String getId() {
        return id;
    }

    public int getElectionId() {
        return electionId;
    }

    public String getOptionName() {
        return optionName;
    }

    public String getDescription() {
        return description;
    }

    public String getLogoPath() {
        return logoPath;
    }
}
