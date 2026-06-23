package com.example.edushare.feature.notification;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.base.RetrofitClient;
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

    public void getUserNotification(String userId, OnActionCallback<List<NotificationResponseDto>> callback) {
        notificationApi.getUserNotifications(userId).enqueue(new Callback<List<NotificationResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<NotificationResponseDto>> call,@NonNull Response<List<NotificationResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Loi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<NotificationResponseDto>> call, Throwable t) {
                callback.onFailure("Loi ket noi: " + t.getMessage());
            }
        });
    }

}
