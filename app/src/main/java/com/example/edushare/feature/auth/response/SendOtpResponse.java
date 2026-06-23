package com.example.edushare.feature.auth.response;

public class SendOtpResponse {
    private String message;
    private String email;
    private boolean success;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
