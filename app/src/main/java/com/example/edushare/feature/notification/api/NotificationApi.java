package com.example.edushare.feature.notification.api;

import com.example.edushare.feature.chat.message.dto.PageResponse;
import com.example.edushare.feature.notification.dto.NotificationResponseDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface NotificationApi {
    @GET("/api/notifications")
    Call<PageResponse<NotificationResponseDto>> getUserNotifications(
            @Query("userId") String userId,
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("api/notifications/mark-all-read")
    Call<Void> markNotificationsAsRead(@Query("userId") String userId);

    @GET("/api/notifications/check-unread")
    Call<Boolean> checkUnread(@Query("userId") String userId);
}
