package com.example.edushare.feature.post.api;

import com.example.edushare.feature.item_detail.model.PostDetailResponseDTO;
import com.example.edushare.feature.post.model.CreatePostRequest;
import com.example.edushare.feature.post.model.PostResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PostApi {
    @POST("/posts")
    Call<PostResponse> createPost(@Body CreatePostRequest request);

    @GET("/posts/detail")
    Call<PostDetailResponseDTO> getPostDetail(@Query("postId") Integer postId);

    @PUT("/posts/{postId}")
    Call<PostResponse> updatePost(@Path("postId") Integer postId, @Body CreatePostRequest request);
}