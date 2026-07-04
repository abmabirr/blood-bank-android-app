package com.bloodbank_2026.bloodbank;

public class Notice {
    public String noticeId, title, message, timestamp;

    public Notice() { }

    public Notice(String noticeId, String title, String message, String timestamp) {
        this.noticeId = noticeId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }
}
