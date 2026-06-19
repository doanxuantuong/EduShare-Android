package com.example.edushare.feature.chat.message.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MessageRequest implements Serializable {
    @SerializedName("conversationId")
    private Integer conversationId;

    @SerializedName("postId")
    private Integer postId;

    @SerializedName("senderId")
    private String senderId;

    @SerializedName("recipientId")
    private String recipientId;

    @SerializedName("content")
    private String content;

    public MessageRequest() {
    }

    public MessageRequest(Integer conversationId, Integer postId, String senderId, String recipientId, String content) {
        this.conversationId = conversationId;
        this.postId = postId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
