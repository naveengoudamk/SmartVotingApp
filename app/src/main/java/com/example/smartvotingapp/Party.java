package com.example.smartvotingapp;

public class Party {
    private String id;
    private String name;
    private String symbol;
    private String description;
    private String logoPath;

    // No-argument constructor required by Firebase
    public Party() {
    }

    public Party(String id, String name, String symbol, String description, String logoPath) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.description = description;
        this.logoPath = logoPath;
    }

    // Constructor for backward compatibility or when logo is not provided
    public Party(String id, String name, String symbol, String description) {
        this(id, name, symbol, description, null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }
}
