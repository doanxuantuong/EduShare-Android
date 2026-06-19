package com.example.edushare.feature.notification.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.edushare.feature.notification.manager.FcmTokenManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.e("FCM_TOKEN", "New token: " + token);

        FcmTokenManager fcmTokenManager = new FcmTokenManager(this);

        // Lưu local trước
        fcmTokenManager.saveTokenLocal(token);

        // Nếu app đã từng lưu userId thì gửi token mới lên backend luôn
        String currentUserId = fcmTokenManager.getCurrentUserId();

        if (currentUserId != null) {
            fcmTokenManager.saveTokenToServer(currentUserId, token);
        } else {
            Log.e("FCM_TOKEN", "Chưa có userId, token sẽ được gửi sau khi user mở app/login");
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d("FCM_MESSAGE", "Có notification mới");

        if (message.getNotification() != null) {
            Log.d("FCM_MESSAGE", "Title: " + message.getNotification().getTitle());
            Log.d("FCM_MESSAGE", "Body: " + message.getNotification().getBody());
        }

        if (!message.getData().isEmpty()) {
            Log.d("FCM_MESSAGE", "Data: " + message.getData());
        }
    }
}
