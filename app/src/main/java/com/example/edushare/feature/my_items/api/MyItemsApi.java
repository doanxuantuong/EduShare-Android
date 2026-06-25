package com.example.edushare.feature.my_items.api;

import com.example.edushare.feature.home.model.PageResponse;
import com.example.edushare.feature.home.model.PostListItemResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MyItemsApi {
    @GET("/posts/user/{userId}")
    Call<PageResponse<PostListItemResponse>> getMyPosts(
            @Path("userId") String userId,
            @Query("page") int page,
            @Query("size") int size
    );
}