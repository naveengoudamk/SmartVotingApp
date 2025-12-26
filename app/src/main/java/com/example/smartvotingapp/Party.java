package com.example.smartvotingapp;

public class Party {
    private String id;
    private String name;
    private String symbol;
    private String description;
    private String logoPath;

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

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    public String getLogoPath() {
        return logoPath;
    }
}
