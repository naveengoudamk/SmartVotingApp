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
    private boolean deleted = false; // Soft delete flag

    public User() {
        // Required for Firebase
    }

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

    public String getAadhaarId() {
        return aadhaarId;
    }

    public void setAadhaarId(String aadhaarId) {
        this.aadhaarId = aadhaarId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
