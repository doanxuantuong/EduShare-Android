package com.example.edushare.feature.item_detail.api;

import com.example.edushare.feature.home.model.PostListItemResponse;
import com.example.edushare.feature.item_detail.model.PostDetailResponseDTO;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
public interface PostDetailApi {
    @GET("posts/detail")
    Call<PostDetailResponseDTO> getDetailPost(@Query("postId") Integer postId);
}

