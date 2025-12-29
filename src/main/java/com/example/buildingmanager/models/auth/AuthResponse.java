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

    // Thêm các thông tin này để React dùng
    private Long id;
    private String username;
    private String email;
    private List<String> roles;

    // Constructor custom để code trong Controller ngắn gọn hơn
    public AuthResponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}