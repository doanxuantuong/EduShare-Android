package com.example.edushare.feature.post.model;

public class CreatePostRequest {
    private final String title;
    private final String description;
    private final String condition;
    private final Double price;
    private final String imageUrl;
    private final String categoryName;
    private final Integer locationId;
    private final String itemType;
    private final String transactionType;

    public CreatePostRequest(
            String title,
            String description,
            String condition,
            Double price,
            String imageUrl,
            String categoryName,
            Integer locationId,
            String itemType,
            String transactionType
    ) {
        this.title = title;
        this.description = description;
        this.condition = condition;
        this.price = price;
        this.imageUrl = imageUrl;
        this.categoryName = categoryName;
        this.locationId = locationId;
        this.itemType = itemType;
        this.transactionType = transactionType;
    }
}