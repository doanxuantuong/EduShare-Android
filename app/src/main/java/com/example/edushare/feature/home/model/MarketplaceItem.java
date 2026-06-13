package com.example.edushare.feature.home.model;

public class MarketplaceItem {
    private final int imageResource;
    private final String condition;
    private final String title;
    private final String location;
    private final String price;
    private final String category;
    private final boolean verified;

    public MarketplaceItem(
            int imageResource,
            String condition,
            String title,
            String location,
            String price,
            String category,
            boolean verified
    ) {
        this.imageResource = imageResource;
        this.condition = condition;
        this.title = title;
        this.location = location;
        this.price = price;
        this.category = category;
        this.verified = verified;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getCondition() {
        return condition;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public boolean isVerified() {
        return verified;
    }
}
