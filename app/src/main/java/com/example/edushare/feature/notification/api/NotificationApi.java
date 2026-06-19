package com.example.edushare.feature.notification.api;

import com.example.edushare.feature.notification.dto.request.SaveFcmTokenRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NotificationApi {
    @POST("fcm/tokens")
    Call<Void> saveFcmToken(@Body SaveFcmTokenRequest request);

}
