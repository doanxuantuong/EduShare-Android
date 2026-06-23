// feature/locations/model/PostMapDto.java
package com.example.edushare.feature.locations.model;

import com.google.gson.annotations.SerializedName;

public class PostMapDto {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("price")
    private double price;

    @SerializedName("status")
    private String status;

    @SerializedName("areaName")
    private String areaName;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    // Convert sang ItemLocation để dùng lại toàn bộ logic cũ
    public ItemLocation toItemLocation() {
        return new ItemLocation(id, title, areaName, latitude, longitude, description, imageUrl);
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getAreaName() { return areaName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}