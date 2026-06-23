package com.example.edushare.feature.home;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.base.RetrofitClient;
import com.example.edushare.feature.home.api.HomeApi;
import com.example.edushare.feature.home.model.PostListItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepository {
    private final HomeApi homeApi;

    public HomeRepository(Context context) {
        this.homeApi = RetrofitClient.createService(HomeApi.class, context);
    }

    public void getPosts(OnActionCallback<List<PostListItemResponse>> callback) {
        homeApi.getPosts().enqueue(new Callback<List<PostListItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<PostListItemResponse>> call,
                                   @NonNull Response<List<PostListItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Loi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PostListItemResponse>> call, @NonNull Throwable t) {
                callback.onFailure("Loi ket noi: " + t.getMessage());
            }
        });
    }
}
