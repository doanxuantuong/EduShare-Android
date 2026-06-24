package com.example.edushare.feature.users;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.common.base.retrofit.RetrofitClient;
import com.example.edushare.feature.auth.request.LoginRequest;
import com.example.edushare.feature.auth.request.OcrLoginRequest;
import com.example.edushare.feature.auth.request.RegisterRequest;
import com.example.edushare.feature.auth.request.SendOtpRequest;
import com.example.edushare.feature.auth.response.LoginResponse;
import com.example.edushare.feature.auth.response.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final UserApi userApi;

    public UserRepository(Context context) {
        this.userApi = RetrofitClient.createService(UserApi.class, context);
    }

    public void register(RegisterRequest request, OnActionCallback<String> callback) {
        userApi.registerUser(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getMessage());
                } else {
                    callback.onFailure("Lỗi đăng ký: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void sendOtp(SendOtpRequest request, OnActionCallback<String> callback) {
        userApi.sendOtp(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Đọc chuỗi message từ JSON hợp lệ gửi về
                    callback.onSuccess(response.body().getMessage());
                } else {
                    callback.onFailure("Lỗi gửi OTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void login(LoginRequest request, OnActionCallback<LoginResponse> callback) {
        userApi.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call,
                                   @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Lỗi đăng nhập: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // này là làm để được như session bên web bay chỉ việc lấy ra dùng
    public void getCurrentUser(String token, OnActionCallback<User> callback) {
        userApi.getCurrentUser("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Lỗi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void loginUserByOcr(OcrLoginRequest request, OnActionCallback<LoginResponse> callback) {
        userApi.loginUserByOcr(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call,@NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Đăng nhập OCR thất bại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                callback.onFailure("Không thể kết nối đến máy chủ");
            }
        });
    }


}
