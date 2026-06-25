package com.example.edushare.feature.post.model;

public class PostResponse {
    private Integer id;
    private String title;
    private String description;
    private Double price;
    private String imageUrl;
    private String status;
    private Integer categoryId;
    private String categoryName;
    private Integer locationId;
    private String locationName;
    private String authorId;
    private String authorName;

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }
}