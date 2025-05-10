package com.healthyassistant.backend.dto;

import com.healthyassistant.backend.model.UserRecord;

public class AddRecordRequest {
    private UserRecord userRecord;
    private String username;

    // Getters and Setters
    public UserRecord getUserRecord() {
        return userRecord;
    }

    public void setUserRecord(UserRecord userRecord) {
        this.userRecord = userRecord;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
