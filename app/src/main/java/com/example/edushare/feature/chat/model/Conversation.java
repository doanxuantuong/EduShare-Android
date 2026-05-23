package com.example.edushare.feature.chat.model;

public class Conversation {
    private int id;
    private String partnerId;
    private String partnerName;
    private String partnerAvatarUrl;
    private String lastMessage;
    private String updatedAt;

    public Conversation(int id, String partnerId, String partnerName, String partnerAvatarUrl, String lastMessage, String updatedAt) {
        this.id = id;
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.partnerAvatarUrl = partnerAvatarUrl;
        this.lastMessage = lastMessage;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
}
