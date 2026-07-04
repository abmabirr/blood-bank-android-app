package com.bloodbank_2026.bloodbank;

public class Donor {
    public String name, blood, location, phone, uid;

    public Donor() {
        // Required for Firebase
    }

    public Donor(String name, String blood, String location, String phone, String uid) {
        this.name = name;
        this.blood = blood;
        this.location = location;
        this.phone = phone;
        this.uid = uid;
    }
}