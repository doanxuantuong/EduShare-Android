package com.example.edushare.feature.home;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.common.base.retrofit.RetrofitClient;
import com.example.edushare.feature.home.api.HomeApi;
import com.example.edushare.feature.home.model.PageResponse;
import com.example.edushare.feature.home.model.PostListItemResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepository {
    private final HomeApi homeApi;

    public HomeRepository(Context context) {
        this.homeApi = RetrofitClient.createService(HomeApi.class, context);
    }

    public void getPosts(
            int page,
            int size,
            String category,
            String keyword,
            OnActionCallback<PageResponse<PostListItemResponse>> callback
    ) {
        homeApi.getPosts(page, size, category, keyword).enqueue(new Callback<PageResponse<PostListItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<PostListItemResponse>> call,
                                   @NonNull Response<PageResponse<PostListItemResponse>> response) {
                Log.d("HOME_API", "URL: " + call.request().url());
                Log.d("HOME_API", "CODE: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<PostListItemResponse> body = response.body();
                    int postCount = body.getContent() == null ? 0 : body.getContent().size();
                    Log.d("HOME_API", "POST SIZE: " + postCount);
                    Log.d("HOME_API", "IS LAST: " + body.isLast());
                    callback.onSuccess(body);
                    return;
                }

                String errorBody = "";
                try {
                    if (response.errorBody() != null) {
                        errorBody = response.errorBody().string();
                    }
                } catch (Exception e) {
                    Log.e("HOME_API", "Cannot read error body", e);
                }

                Log.e("HOME_API", "ERROR BODY: " + errorBody);
                callback.onFailure("Cannot load posts. Server code: " + response.code());
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<PostListItemResponse>> call,
                                  @NonNull Throwable t) {
                Log.e("HOME_API", "FAILED URL: " + call.request().url(), t);
                callback.onFailure("Cannot connect to post service");
            }
        });
    }
}
