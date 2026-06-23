package com.example.edushare.feature.item_detail.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.base.RetrofitClient;
import com.example.edushare.feature.chat.ChatApi;
import com.example.edushare.feature.item_detail.api.PostDetailApi;
import com.example.edushare.feature.item_detail.model.PostDetailResponseDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class PostDetailRepository {
    private final PostDetailApi postDetailApi;

    public PostDetailRepository(Context context) {
        this.postDetailApi = RetrofitClient.createService(PostDetailApi.class, context);
    }

    // Thêm hàm call API chi tiết bài viết
    public void getPostDetail(Integer postId, OnActionCallback<PostDetailResponseDTO> callback) {
        postDetailApi.getDetailPost(postId).enqueue(new Callback<PostDetailResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<PostDetailResponseDTO> call,
                                   @NonNull Response<PostDetailResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Lỗi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostDetailResponseDTO> call, @NonNull Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
