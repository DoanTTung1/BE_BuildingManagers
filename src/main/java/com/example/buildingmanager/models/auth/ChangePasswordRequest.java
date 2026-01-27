package com.example.buildingmanager.models.auth;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String currentPassword; // Mật khẩu hiện tại
    private String newPassword;     // Mật khẩu mới
    private String confirmPassword; // Nhập lại mật khẩu mới
}