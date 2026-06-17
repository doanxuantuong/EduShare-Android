package com.example.edushare.feature.chat.message.dto;

import java.io.Serializable;
import java.sql.Timestamp;

public class MessageResponse implements Serializable {
    private int id;
    private int conversationId;
    private String senderId;
    private String content;
    private Boolean isRead;

    //Bên ANROID khi nhận sẽ nhận dạng JSON
    //Nền cần nhận dạng String thay vì Timestamp
    //do có thể bên JSON trả về dạng khác nhau: "createdAt": [2026, 6, 17, 14, 43, 22]
    private String createdAt;

    public MessageResponse() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}