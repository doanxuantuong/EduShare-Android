package com.example.edushare.feature.home.api;

import com.example.edushare.feature.home.model.PageResponse;
import com.example.edushare.feature.home.model.PostListItemResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HomeApi {
    @GET("/posts")
    Call<PageResponse<PostListItemResponse>> getPosts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("category") String category,
            @Query("keyword") String keyword
    );
}
