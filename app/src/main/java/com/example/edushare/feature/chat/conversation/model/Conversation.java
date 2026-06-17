package com.example.edushare.feature.chat.conversation.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Conversation implements Serializable {
    @SerializedName("conversationId")
    private Integer id;
    private String recipientId;
    private String recipientName;
    private String recipientAvatarUrl;
    private String lastMessage;
    private String updatedAt;

    public Conversation() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientAvatarUrl() {
        return recipientAvatarUrl;
    }

    public void setRecipientAvatarUrl(String recipientAvatarUrl) {
        this.recipientAvatarUrl = recipientAvatarUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", recipientId='" + recipientId + '\'' +
                ", recipientName='" + recipientName + '\'' +
                ", recipientAvatarUrl='" + recipientAvatarUrl + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
