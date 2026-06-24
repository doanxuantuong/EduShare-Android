package com.example.edushare.feature.users.dto.request;

public class UserProfileDetailRequest {
    private String fullName;
    private String studentCode;
    private String phone;
    private String avatarUrl;

    public UserProfileDetailRequest() {
    }

    public UserProfileDetailRequest(String fullName, String studentCode, String phone, String avatarUrl) {
        this.fullName = fullName;
        this.studentCode = studentCode;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}