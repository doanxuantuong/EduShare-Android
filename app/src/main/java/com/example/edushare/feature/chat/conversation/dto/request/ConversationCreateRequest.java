package com.example.edushare.feature.chat.conversation.dto.request;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ConversationCreateRequest implements Serializable {
    @SerializedName("senderId")
    private String senderId;
    @SerializedName("recipientId")
    private String recipientId;
    @SerializedName("postId")
    private int postId;
    @SerializedName("content")
    private String content;

    public ConversationCreateRequest(String senderId, String recipientId, int postId, String content) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.postId = postId;
        this.content = content;
    }

    public ConversationCreateRequest() {
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
