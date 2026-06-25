package com.example.edushare.feature.post.api;

import com.example.edushare.feature.post.model.CreatePostRequest;
import com.example.edushare.feature.post.model.PostResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PostApi {
    @POST("/posts")
    Call<PostResponse> createPost(@Body CreatePostRequest request);
}