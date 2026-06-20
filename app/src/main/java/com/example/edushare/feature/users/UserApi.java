package com.example.edushare.feature.users;

import com.example.edushare.feature.auth.request.LoginRequest;
import com.example.edushare.feature.auth.request.OcrLoginRequest;
import com.example.edushare.feature.auth.request.RegisterRequest;
import com.example.edushare.feature.auth.response.LoginResponse;
import com.example.edushare.feature.auth.response.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserApi {
    @POST("api/auth/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @POST("api/auth/ocr-login")
    Call<LoginResponse> loginUserByOcr(@Body OcrLoginRequest request);

    // này giống session bên web tụi bay gọi lại là đc
    @GET("api/users/me")
    Call<User> getCurrentUser(@Header("Authorization") String token);

    @GET("api/users/{id}")
    Call<User> getUserProfile(@Path("id") String userId);


}

