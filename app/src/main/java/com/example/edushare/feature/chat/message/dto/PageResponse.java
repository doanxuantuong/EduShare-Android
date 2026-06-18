package com.example.edushare.feature.chat.message.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PageResponse<T> {
    @SerializedName("content")
    private List<T> content;

    @SerializedName("last")
    private boolean last; // Đánh dấu đã hết tin nhắn cũ chưa

    public List<T> getContent() {
        return content;
    }

    public boolean isLast() {
        return last;
    }
}
