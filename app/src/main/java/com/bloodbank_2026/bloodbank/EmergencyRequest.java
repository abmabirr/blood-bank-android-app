package com.bloodbank_2026.bloodbank;

public class EmergencyRequest {
    public String requestId, bloodGroup, location, phone, message, timestamp;

    public EmergencyRequest() { }

    public EmergencyRequest(String requestId, String bloodGroup, String location, String phone, String message, String timestamp) {
        this.requestId = requestId;
        this.bloodGroup = bloodGroup;
        this.location = location;
        this.phone = phone;
        this.message = message;
        this.timestamp = timestamp;
    }
}
