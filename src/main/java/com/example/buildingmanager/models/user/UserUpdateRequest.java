package com.example.buildingmanager.models.user;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserUpdateRequest {
    private String fullName;
    private String email;
    private String phone; // Cho phép sửa SĐT
    private String username; // Cho phép sửa tên đăng nhập
    private MultipartFile avatarFile; // Trường này để hứng file ảnh từ React gửi lên
    private String newPassword; // Mật khẩu mới (nếu có)
}