package com.example.edushare.feature.notification.repository;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.base.RetrofitClient;
import com.example.edushare.feature.notification.api.NotificationApi;
import com.example.edushare.feature.notification.dto.request.SaveFcmTokenRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {
    private final NotificationApi notificationApi;

    public NotificationRepository() {
        // RetrofitClient.createService: Sử dụng kỹ thuật Dynamic Proxy.
        // Retrofit sẽ tự tạo ra một class thực thi cho interface ChatApi lúc Runtime.
        // Giống như việc Spring tự tạo Implementation cho các JpaRepository.
        this.notificationApi = RetrofitClient.createService(NotificationApi.class);
    }

    public void saveFcmToken(String userId, String fcmToken, OnActionCallback<Void> callback) {
        SaveFcmTokenRequest request = new SaveFcmTokenRequest(userId, fcmToken);

        notificationApi.saveFcmToken(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure("Lưu FCM token thất bại: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFailure("Lỗi kết nối khi lưu FCM token: " + t.getMessage());
            }
        });
    }
}
