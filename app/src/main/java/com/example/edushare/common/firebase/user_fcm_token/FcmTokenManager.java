package com.example.edushare.common.firebase.user_fcm_token;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.firebase.user_fcm_token.repository.UserFcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;

public class FcmTokenManager {
    private static final String PREF_NAME = "notification_pref";
    // => Tên của file cấu hình ngầm (SharedPreferences) lưu trong bộ nhớ điện thoại.
    private static final String KEY_CURRENT_USER_ID = "current_user_id";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    // => KEY_CURRENT_USER_ID & KEY_FCM_TOKEN2 cái "chìa khóa" dùng để cất và lấy dữ liệu từ cái file cấu hình bên trên.

    private final Context context;
    // => Môi trường hoạt động của android, phải có nó thì mới mở được SharedPreferences
    private final UserFcmTokenRepository notificationRepository;

    public FcmTokenManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationRepository = new UserFcmTokenRepository(this.context);
    }

    // ==============================================================================
    // CÁC HÀM CƠ BẢN CHO FILE CẤU HÌNH
    // ==============================================================================

    // Lưu userId hiện tại để MyFirebaseMessagingService có thể lấy lại khi token đổi
    public void saveCurrentUserId(String userId) {
        getPreferences()
                .edit()
                .putString(KEY_CURRENT_USER_ID, userId)
                .apply();
    }

    // Lấy ID người dùng
    public String getCurrentUserId() {
        return getPreferences().getString(KEY_CURRENT_USER_ID, null);
    }

    // Lưu FCM Token
    public void saveTokenLocal(String token) {
        getPreferences()
                .edit()
                .putString(KEY_FCM_TOKEN, token)
                .apply();
    }

    // Lấy FCM Token
    public String getTokenLocal() {
        return getPreferences().getString(KEY_FCM_TOKEN, null);
    }

    // Mở file cấu hình
    private SharedPreferences getPreferences() {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ==============================================================================
    // CÁC HÀM CHÍNH LƯU LẠI TOKEN CHO CẢ DB, LOCAL VÀ SERVER
    // ==============================================================================

    // Dùng ở Activity sau khi biết userId
    // Được dùng khi user đăng nhập thành công hoặc mở app lên
    public void getAndSaveFcmToken(String userId) {
        saveCurrentUserId(userId);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("FCM_TOKEN", "Lấy FCM token thất bại", task.getException());
                return;
            }

            String token = task.getResult();
            Log.e("FCM_TOKEN", "Token: " + token);

            // Lưu 1 cái xuống Local
            saveTokenLocal(token);
            // Lưu xuống Server
            saveTokenToServer(userId, token);
        });
    }

    // Dùng chung cho cả Activity và MyFirebaseMessagingService
    public void saveTokenToServer(String userId, String token) {
        if (userId == null || userId.trim().isEmpty()) {
            Log.e("FCM_TOKEN", "Không lưu token được vì userId null");
            return;
        }

        if (token == null || token.trim().isEmpty()) {
            Log.e("FCM_TOKEN", "Không lưu token được vì token null");
            return;
        }

        notificationRepository.saveFcmToken(userId, token, new OnActionCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.e("FCM_TOKEN", "Lưu FCM token lên backend thành công");
            }

            @Override
            public void onFailure(String error) {
                Log.e("FCM_TOKEN", error);
            }
        });
    }
}
