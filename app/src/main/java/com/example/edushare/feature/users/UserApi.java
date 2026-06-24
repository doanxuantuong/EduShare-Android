package com.example.edushare.feature.users;

import com.example.edushare.feature.auth.request.LoginRequest;
import com.example.edushare.feature.auth.request.OcrLoginRequest;
import com.example.edushare.feature.auth.request.RegisterRequest;
import com.example.edushare.feature.auth.request.SendOtpRequest;
import com.example.edushare.feature.auth.response.LoginResponse;
import com.example.edushare.feature.auth.response.RegisterResponse;
import com.example.edushare.feature.users.dto.request.UserProfileDetailRequest;
import com.example.edushare.feature.users.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface UserApi {

    @POST("api/auth/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);


    @POST("api/auth/send-otp")
    Call<RegisterResponse> sendOtp(@Body SendOtpRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @POST("api/auth/ocr-login")
    Call<LoginResponse> loginUserByOcr(@Body OcrLoginRequest request);

    @GET("api/users/me")
    Call<User> getCurrentUser(@Header("Authorization") String token);

    // API Cập nhật profile chi tiết
    @PUT("api/users/{id}")
    Call<User> updateProfile(@retrofit2.http.Path("id") String id, @Body UserProfileDetailRequest request
    );
}

