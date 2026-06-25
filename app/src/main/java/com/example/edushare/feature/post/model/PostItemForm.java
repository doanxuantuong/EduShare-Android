package com.example.edushare.feature.post.model;

/**
 * Bản chụp dữ liệu hiện tại của form Post, dùng cho validate và mapping payload API sau này.
 */
public class PostItemForm {
    private final String itemType;
    private final String title;
    private final String category;
    private final String condition;
    private final String price;
    private final int locationId;
    private final String location;
    private final String description;

    public PostItemForm(
            String itemType,
            String title,
            String category,
            String condition,
            String price,
            int locationId,
            String location,
            String description
    ) {
        this.itemType = itemType;
        this.title = title;
        this.category = category;
        this.condition = condition;
        this.price = price;
        this.locationId = locationId;
        this.location = location;
        this.description = description;
    }

    public String getItemType() {
        return itemType;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getCondition() {
        return condition;
    }

    public String getPrice() {
        return price;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }
}