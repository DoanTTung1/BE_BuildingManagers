package com.example.buildingmanager.controllers.auth;

import com.example.buildingmanager.models.auth.LoginRequest;
import com.example.buildingmanager.models.auth.RegisterRequest;
import com.example.buildingmanager.models.auth.OtpVerificationRequest;
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
            // Lưu ý: Trong AuthServiceImpl, hãy đảm bảo hàm này không bị nghẽn Transaction
            return ResponseEntity.ok(authService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Gửi OTP
    // SỬA: Chỉ định rõ "userName" để khớp với toàn bộ hệ thống của bạn
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam("userName") String userName) {
        try {
            authService.sendOtp(userName);
            return ResponseEntity.ok("Mã OTP đã được gửi!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Xác thực OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        try {
            // SỬA: Phải gọi .getUserName() (viết hoa N) để khớp với DTO
            // OtpVerificationRequest
            boolean isVerified = authService.verifyOtp(request.getUserName(), request.getOtp());
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