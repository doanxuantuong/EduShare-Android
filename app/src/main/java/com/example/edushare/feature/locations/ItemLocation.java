package com.example.edushare.feature.locations;

public class ItemLocation {
    private int id;
    private String name;        // tên đồ vật (title của post)
    private String areaName;    // Dorm A, Dorm B, Canteen,...
    private double latitude;
    private double longitude;
    private String description;
    private String imageUrl;

    public ItemLocation(int id, String name, String areaName,
                        double latitude, double longitude,
                        String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.areaName = areaName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAreaName() { return areaName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
}