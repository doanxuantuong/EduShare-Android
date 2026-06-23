package com.example.edushare.feature.auth.request;

public class RegisterRequest {
    private String fullName;
    private String email;
    private String phone;
    private String studentCode;
    private String password;
    private String confirmPassword; // Trường bắt buộc để BE đối chiếu
    private String otp;

    // Constructor đầy đủ tham số gán dữ liệu chính xác
    public RegisterRequest(String fullName, String email, String phone, String studentCode, String password, String confirmPassword, String otp) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.studentCode = studentCode;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.otp = otp;
    }

    // Getter và Setter
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}