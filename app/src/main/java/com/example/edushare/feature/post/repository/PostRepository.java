package com.example.edushare.feature.post.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.common.base.retrofit.RetrofitClient;
import com.example.edushare.feature.item_detail.model.PostDetailResponseDTO;
import com.example.edushare.feature.post.api.PostApi;
import com.example.edushare.feature.post.model.CreatePostRequest;
import com.example.edushare.feature.post.model.PostResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRepository {
    private final PostApi postApi;

    public PostRepository(Context context) {
        this.postApi = RetrofitClient.createService(PostApi.class, context);
    }

    public void createPost(CreatePostRequest request, OnActionCallback<PostResponse> callback) {
        postApi.createPost(request).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(@NonNull Call<PostResponse> call, @NonNull Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    return;
                }

                callback.onFailure(readErrorMessage(response));
            }

            @Override
            public void onFailure(@NonNull Call<PostResponse> call, @NonNull Throwable t) {
                callback.onFailure("Loi ket noi: " + t.getMessage());
            }
        });
    }

    public void updatePost(Integer postId, CreatePostRequest request, OnActionCallback<PostResponse> callback) {
        postApi.updatePost(postId, request).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(@NonNull Call<PostResponse> call, @NonNull Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    return;
                }

                callback.onFailure(readErrorMessage(response));
            }

            @Override
            public void onFailure(@NonNull Call<PostResponse> call, @NonNull Throwable t) {
                callback.onFailure("Loi ket noi: " + t.getMessage());
            }
        });
    }

    public void getPostDetail(Integer postId, OnActionCallback<PostDetailResponseDTO> callback) {
        postApi.getPostDetail(postId).enqueue(new Callback<PostDetailResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<PostDetailResponseDTO> call, @NonNull Response<PostDetailResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    return;
                }

                callback.onFailure(readErrorMessage(response));
            }

            @Override
            public void onFailure(@NonNull Call<PostDetailResponseDTO> call, @NonNull Throwable t) {
                callback.onFailure("Loi ket noi: " + t.getMessage());
            }
        });
    }


    public void deletePost(Integer postId, OnActionCallback<Void> callback) {
        postApi.deletePost(postId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                    return;
                }

                callback.onFailure(readErrorMessage(response));
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onFailure("Loi ket noi: " + t.getMessage());
            }
        });
    }
    private String readErrorMessage(Response<?> response) {
        if (response.errorBody() == null) {
            return "Loi Server: " + response.code();
        }

        try {
            String message = response.errorBody().string();
            if (message == null || message.trim().isEmpty()) {
                return "Loi Server: " + response.code();
            }
            return message;
        } catch (IOException e) {
            return "Loi Server: " + response.code();
        }
    }
}