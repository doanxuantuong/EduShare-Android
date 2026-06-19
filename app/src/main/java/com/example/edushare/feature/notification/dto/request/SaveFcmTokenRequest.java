package com.example.edushare.feature.notification.dto.request;

import java.io.Serializable;

public class SaveFcmTokenRequest implements Serializable {
    private String userId;
    private String fcmToken;

    public SaveFcmTokenRequest(String userId, String fcmToken) {
        this.userId = userId;
        this.fcmToken = fcmToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }
}
