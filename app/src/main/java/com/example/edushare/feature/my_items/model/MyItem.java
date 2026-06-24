package com.example.edushare.feature.my_items.model;

/**
 * Model tối giản cho một item của chính người dùng trên màn My Items.
 * Các field chỉ phục vụ UI hiện tại; khi API ổn định có thể map từ DTO sang model này.
 */public class MyItem {
    private final int id;
    private final int imageResource;
    private final String imageUrl;
    private final String title;
    private final String priceLabel;
    private final int viewCount;
    private final int likeCount;
    private final boolean sold;

    public MyItem(
            int id,
            int imageResource,
            String imageUrl,
            String title,
            String priceLabel,
            int viewCount,
            int likeCount,
            boolean sold
    ) {
        this.id = id;
        this.imageResource = imageResource;
        this.imageUrl = imageUrl;
        this.title = title;
        this.priceLabel = priceLabel;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.sold = sold;
    }

    public int getId() {
        return id;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getPriceLabel() {
        return priceLabel;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public boolean isSold() {
        return sold;
    }
}
