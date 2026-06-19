package com.example.edushare.feature.notification.user_fcm_token.api;

import com.example.edushare.feature.notification.user_fcm_token.dto.SaveFcmTokenRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserFcmTokenApi {
    // Chỉ có tác dụng lưu FcmToken được bên Firebase cung cấp
    // Bên Server sẽ lưu lại cái token này vào DB
    @POST("fcm/tokens")
    Call<Void> saveFcmToken(@Body SaveFcmTokenRequest request);

}
