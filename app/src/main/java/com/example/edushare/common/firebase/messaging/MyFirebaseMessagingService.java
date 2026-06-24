package com.example.edushare.common.firebase.messaging;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.edushare.common.firebase.user_fcm_token.FcmTokenManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

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

        Log.e("FCM_TOKEN", "New token: " + message);

        Map<String, String> data = new HashMap<>();
        if (!data.isEmpty()) {
            String notificationType = data.get("type");

            if (notificationType != null) {
                switch (notificationType) {
                    case "CHAT":
                        String conversationId = data.get("conversationId");
                        Log.d("FCM", "Xử lý logic hiện thông báo Chat cho phòng: " + conversationId);
                        // Vẽ thông báo Chat, bấm vào mở màn hình ChatDetailActivity
                        break;

                    case "SYSTEM":
                        // Xử lý thông báo từ nhà trường / admin hệ thống...
                        break;
                }
            }
        }
    }
}
