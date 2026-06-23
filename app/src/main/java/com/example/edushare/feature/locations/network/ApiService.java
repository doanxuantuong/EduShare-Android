// network/ApiService.java
package com.example.edushare.feature.locations.network;

import com.example.edushare.feature.locations.model.PostMapDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("posts/map")
    Call<List<PostMapDto>> getPostsForMap(
            @Query("area")    String area,
            @Query("keyword") String keyword
    );
}