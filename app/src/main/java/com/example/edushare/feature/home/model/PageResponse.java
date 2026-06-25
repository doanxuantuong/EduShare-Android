package com.example.edushare.feature.home.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PageResponse<T> {
    @SerializedName("content")
    private List<T> content;

    @SerializedName("last")
    private boolean last;

    @SerializedName("number")
    private int number;

    @SerializedName("totalElements")
    private long totalElements;

    public List<T> getContent() {
        return content;
    }

    public boolean isLast() {
        return last;
    }

    public int getNumber() {
        return number;
    }

    public long getTotalElements() {
        return totalElements;
    }
}
