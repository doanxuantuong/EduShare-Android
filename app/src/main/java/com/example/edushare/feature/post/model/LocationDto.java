package com.example.edushare.feature.post.model;

import com.google.gson.annotations.SerializedName;

public class LocationDto {
    @SerializedName("id")
    private int id;

    @SerializedName("areaName")
    private String areaName;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    public int getId() { return id; }
    public String getAreaName() { return areaName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}