package com.example.edushare.feature.post.model;

import com.google.gson.annotations.SerializedName;

public class CreateLocationRequest {
    @SerializedName("areaName")  private String areaName;
    @SerializedName("latitude")  private double latitude;
    @SerializedName("longitude") private double longitude;

    public CreateLocationRequest(String areaName, double latitude, double longitude) {
        this.areaName  = areaName;
        this.latitude  = latitude;
        this.longitude = longitude;
    }
}