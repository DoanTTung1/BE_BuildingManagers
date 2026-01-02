package com.example.buildingmanager.controllers.users;

import com.example.buildingmanager.models.user.UserDTO;
import com.example.buildingmanager.models.user.UserUpdateRequest;
import com.example.buildingmanager.services.user.IUserService; // Hoặc đường dẫn tới Interface Service của bạn
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAPIController {

    private final IUserService userService;

    /**
     * API Cập nhật thông tin cá nhân (Avatar + Text)
     * URL: POST /api/users/profile/update
     * Content-Type: multipart/form-data
     */
    @PostMapping(value = "/profile/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(@ModelAttribute UserUpdateRequest request) {
        try {
            // 1. Lấy username của người đang đăng nhập (từ Token JWT)
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

            // 2. Gọi Service để xử lý cập nhật (bao gồm cả upload ảnh nếu có)
            UserDTO updatedUser = userService.updateProfile(currentUsername, request);

            // 3. Trả về thông tin User mới đã được update
            return ResponseEntity.ok(updatedUser);

        } catch (RuntimeException e) {
            // Bắt lỗi logic (ví dụ: Trùng username, lỗi upload ảnh...)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Bắt các lỗi hệ thống khác
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}