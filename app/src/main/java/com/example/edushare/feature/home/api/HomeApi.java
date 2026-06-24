package com.example.edushare.feature.home.api;

import com.example.edushare.feature.home.model.PostListItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HomeApi {
    @GET("/posts")
    Call<List<PostListItemResponse>> getPosts();
}
