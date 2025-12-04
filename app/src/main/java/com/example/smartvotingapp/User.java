package com.example.smartvotingapp;

public class User {
    private String aadhaarId;
    private String name;
    private String dob;
    private String email;
    private String mobile;
    private String photo;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private boolean eligible;

    public User(String aadhaarId, String name, String dob, String email,
                String mobile, String photo, String address,
                String city, String state, String pincode, boolean eligible) {
        this.aadhaarId = aadhaarId;
        this.name = name;
        this.dob = dob;
        this.email = email;
        this.mobile = mobile;
        this.photo = photo;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.eligible = eligible;
    }

    public String getAadhaarId() { return aadhaarId; }
    public String getName() { return name; }
    public String getDob() { return dob; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getPhoto() { return photo; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public boolean isEligible() { return eligible; }
}
