package com.example.edushare.feature.chat.message.dto;

import java.io.Serializable;

public class MessageStatusResponse implements Serializable {
    private Integer conversationId;
    private String senderId;
    private Boolean isRead;

    public MessageStatusResponse() {
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }
}
