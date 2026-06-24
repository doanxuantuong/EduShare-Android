package com.example.edushare.feature.transaction.dto.request;

public class TransactionRequestDTO {
    private int postId;
    private String sellerId;
    private String type;
    private String status;

    public TransactionRequestDTO(int postId, String sellerId, String type, String status) {
        this.postId = postId;
        this.sellerId = sellerId;
        this.type = type;
        this.status = status;
    }

    // Getter và Setter (Bắt buộc phải có để Retrofit tự chuyển sang JSON)
    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
