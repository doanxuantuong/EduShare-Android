package com.example.edushare.feature.notification;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.base.RetrofitClient;
import com.example.edushare.feature.chat.message.dto.PageResponse;
import com.example.edushare.feature.notification.dto.NotificationResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {

    private final NotificationApi notificationApi;

    public NotificationRepository(Context context) {
        this.notificationApi = RetrofitClient.createService(NotificationApi.class, context);
    }

    public void getUserNotification(String userId, int page, int size, OnActionCallback<PageResponse<NotificationResponseDto>> callback) {
        notificationApi.getUserNotifications(userId, page, size).enqueue(new Callback<PageResponse<NotificationResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<NotificationResponseDto>> call, @NonNull Response<PageResponse<NotificationResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Loi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PageResponse<NotificationResponseDto>> call, Throwable t) {
                callback.onFailure("Loi ket noi: " + t.getMessage());
            }
        });
    }

    public void markNotificationsAsRead(String userId, OnActionCallback<Void> callback) {
        notificationApi.markNotificationsAsRead(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure("Loi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFailure("Loi ket noi: " + t.getMessage());
            }
        });
    }

    public void checkUnread(String userId, OnActionCallback<Boolean> callback) {
        notificationApi.checkUnread(userId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Trả kết quả true/false qua callback
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Loi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                callback.onFailure("Loi ket noi: " + t.getMessage());
            }
        });
    }

}
