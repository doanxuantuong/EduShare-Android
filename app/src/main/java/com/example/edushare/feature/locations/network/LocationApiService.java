// network/ApiService.java
package com.example.edushare.feature.locations.network;

import com.example.edushare.feature.locations.model.PostMapDto;
import com.example.edushare.feature.post.model.CreateLocationRequest;
import com.example.edushare.feature.post.model.LocationDto;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LocationApiService {
    @GET("posts/map")
    Call<List<PostMapDto>> getPostsForMap(
            @Query("area")    String area,
            @Query("keyword") String keyword
    );
    @POST("locations")
    Call<LocationDto> createLocation(@Body CreateLocationRequest request);
}