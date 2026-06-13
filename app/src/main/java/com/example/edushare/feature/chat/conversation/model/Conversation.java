package com.example.edushare.feature.chat.conversation.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Conversation implements Serializable {
    @SerializedName("conversationId")
    private Integer id;
    private String partnerId;
    private String partnerName;
    private String partnerAvatarUrl;
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

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerAvatarUrl() {
        return partnerAvatarUrl;
    }

    public void setPartnerAvatarUrl(String partnerAvatarUrl) {
        this.partnerAvatarUrl = partnerAvatarUrl;
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
                ", partnerId='" + partnerId + '\'' +
                ", partnerName='" + partnerName + '\'' +
                ", partnerAvatarUrl='" + partnerAvatarUrl + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
