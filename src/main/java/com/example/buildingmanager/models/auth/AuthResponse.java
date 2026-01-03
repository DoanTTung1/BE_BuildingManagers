package com.example.buildingmanager.models.auth;


import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
// @AllArgsConstructor // Có thể bỏ dòng này vì ta đã viết Constructor thủ công
// bên dưới
public class AuthResponse {
    private String token;
    private String type = "Bearer";

    // Các thông tin cho Frontend
    private Long id;
    private String username;

    // --- CÁC TRƯỜNG MỚI BỔ SUNG (QUAN TRỌNG) ---
    private String fullName;
    private String email;
    private String phone;
    private String avatar; 
    // -------------------------------------------

    private List<String> roles;
    private boolean phoneVerified;

    // --- SỬA LẠI CONSTRUCTOR ĐỂ NHẬN ĐỦ THÔNG TIN ---
    public AuthResponse(String token, Long id, String username, String fullName, String email, String phone,
            String avatar, List<String> roles, boolean phoneVerified) {
        this.token = token;
        this.id = id;
        this.username = username;

        // Gán các giá trị mới
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;

        this.roles = roles;
        this.phoneVerified = phoneVerified;
        this.type = "Bearer";
    }
}