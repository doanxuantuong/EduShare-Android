package com.example.edushare.feature.users.model;

public class User {
    private String id;
    private String fullName;
    private String email;

    private String studentCode;
    private String avatarUrl;

    private String phone;

    public User() {
    }

    public User(String id, String fullName, String email, String studentCode, String avatarUrl, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.studentCode = studentCode;
        this.avatarUrl = avatarUrl;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
