package com.example.edushare.feature.chat.message.dto;

import com.google.gson.annotations.SerializedName;

public class PostSummaryResponse {
    @SerializedName("id")
    private Integer id;

    @SerializedName("title")
    private String title;

    @SerializedName("price")
    private Double price;

    @SerializedName("imageUrl")
    private String imageUrl;

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}
