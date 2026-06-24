// network/RetrofitClient.java
package com.example.edushare.feature.locations.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Dùng 10.0.2.2 thay localhost khi chạy trên Android Emulator
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static Retrofit instance;

    public static ApiService getApiService() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance.create(ApiService.class);
    }
}