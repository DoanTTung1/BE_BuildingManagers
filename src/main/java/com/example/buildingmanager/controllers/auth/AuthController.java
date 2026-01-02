package com.example.buildingmanager.controllers.auth;

import com.example.buildingmanager.models.auth.LoginRequest;
import com.example.buildingmanager.models.auth.RegisterRequest;
import com.example.buildingmanager.models.auth.OtpVerificationRequest; // Nhớ tạo file này (xem bên dưới)
import com.example.buildingmanager.services.auth.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    // 1. Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- BỔ SUNG 2 API CHO OTP ---

    // 3. Gửi OTP (Frontend gọi cái này khi bấm nút "Gửi lại mã" hoặc khi vừa vào
    // Modal)
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String username) {
        try {
            authService.sendOtp(username);
            return ResponseEntity.ok("Mã OTP đã được gửi đến số điện thoại!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Xác thực OTP (Frontend gọi cái này khi bấm "Xác nhận")
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        try {
            boolean isVerified = authService.verifyOtp(request.getUsername(), request.getOtp());
            if (isVerified) {
                return ResponseEntity.ok("Xác thực thành công!");
            } else {
                return ResponseEntity.badRequest().body("Xác thực thất bại!");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}