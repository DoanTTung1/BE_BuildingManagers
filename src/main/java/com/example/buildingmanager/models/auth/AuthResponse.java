package com.example.buildingmanager.models.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";

    // Các thông tin cho Frontend
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private boolean phoneVerified; // <--- Trường mới quan trọng

    // --- SỬA LẠI CONSTRUCTOR NÀY ĐỂ NHẬN THÊM phoneVerified ---
    public AuthResponse(String token, Long id, String username, String email, List<String> roles,
            boolean phoneVerified) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.phoneVerified = phoneVerified; // Gán giá trị
        this.type = "Bearer";
    }
}