package com.bloodbank_2026.bloodbank;

public class User {
    public String name, blood, phone, email, location, fcmToken, lastDonationDate;
    public double latitude, longitude;
    public int donationCount;

    public User() {
        // Required for Firebase
    }

    public User(String name, String blood, String phone, String email, String location, double latitude, double longitude, String fcmToken) {
        this.name = name;
        this.blood = blood;
        this.phone = phone;
        this.email = email;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fcmToken = fcmToken;
        this.lastDonationDate = "Never"; // Default value
        this.donationCount = 0;
    }
}
