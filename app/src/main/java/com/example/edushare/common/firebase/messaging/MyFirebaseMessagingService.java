package com.example.edushare.common.firebase.messaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.edushare.R;
import com.example.edushare.common.firebase.user_fcm_token.FcmTokenManager;
import com.example.edushare.feature.chat.conversation.activity.ConversationActivity;
import com.example.edushare.feature.home.activity.HomeActivity;
import com.example.edushare.feature.notification.activity.NotificationActivity;
import com.example.edushare.feature.transaction.activity.TransactionHistoryActivity;
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

        // Lưu fcm-token từ Firebase vào local trước
        fcmTokenManager.saveTokenLocal(token);

        // Nếu app đã từng lưu userId thì gửi token mới lên backend luôn
        String currentUserId = fcmTokenManager.getCurrentUserId();

        if (currentUserId != null) {
            fcmTokenManager.saveTokenToServer(currentUserId, token);
        } else {
            Log.e("FCM_TOKEN", "Chưa có userId, token sẽ được gửi sau khi user mở app/login");
        }
    }

    // Khi app nhận được message từ Firebase.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // Lấy tiêu đề và nội dung notification
        String title = "EduShare";
        String body = "Bạn có thông báo mới";
        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }
        // MẶC ĐỊNH SẼ VÀO HOME
        Intent intent = new Intent(this, HomeActivity.class);

        // Lấy data từ message
        Map<String, String> data = message.getData();
        if (!data.isEmpty()) {
            // Loại thông báo
            String notificationType = data.get("type");
            // ID của bài POST
            String referenceId = data.get("referenceId");
            if (notificationType != null) {
                switch (notificationType) {
                    case "CHAT":
                        // Đưa vào màn hình chat
                        Log.d("FCM", "Định tuyến vào Chat cho phòng: " + referenceId);
                        intent = new Intent(this, ConversationActivity.class);
                        break;

                    case "TRANSACTION":
                        Log.d("FCM", "Định tuyến vào Giao dịch: " + referenceId);
                        intent = new Intent(this, TransactionHistoryActivity.class);
                        break;

                    case "SYSTEM":
                        // Xử lý thông báo hệ thống
                        break;
                }
                intent.putExtra("REFERENCE_ID", referenceId);
            }

            // Nếu Activity đó đã tồn tại trong stack thì đưa nó lên trên
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Nó sẽ đưa cái Intent đó cho Notification
            // Nếu người dùng bấm vào notification thì
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );

            // TẠO KÊNH RIÊNG CHO NOTIFICATION
            String channelId = "edushare_channel_id";
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                    // ICON APP
                    .setSmallIcon(R.drawable.ic_notification_custom)
                    .setContentTitle(title)
                    .setContentText(body)
                    // Khi user bấm vào notification thì notification tự biến mất.
                    .setAutoCancel(true)
                    // Gắn hành động bấm vào notification.
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            // Tạo kênh Notification
            // Lấy service của Android dùng để hiển thị notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Cấp phép kênh thông báo cho Android 8.0 trở lên
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        channelId, "Thông báo EduShare", NotificationManager.IMPORTANCE_HIGH
                );
                notificationManager.createNotificationChannel(channel);
            }

            // Hiển thị notification
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
    }
}
