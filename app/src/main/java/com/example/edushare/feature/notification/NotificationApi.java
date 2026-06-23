package com.example.edushare.feature.notification;

import com.example.edushare.feature.chat.message.dto.PageResponse;
import com.example.edushare.feature.notification.dto.NotificationResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NotificationApi {
    @GET("/api/notifications")
    Call<PageResponse<NotificationResponseDto>> getUserNotifications(
            @Query("userId") String userId,
            @Query("page") int page,
            @Query("size") int size
    );
}
