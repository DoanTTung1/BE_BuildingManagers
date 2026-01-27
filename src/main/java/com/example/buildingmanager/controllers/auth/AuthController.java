package com.example.buildingmanager.controllers.auth;

import com.example.buildingmanager.models.auth.ChangePasswordRequest;
import com.example.buildingmanager.models.auth.LoginRequest;
import com.example.buildingmanager.models.auth.RegisterRequest;
import com.example.buildingmanager.models.auth.OtpVerificationRequest;
import com.example.buildingmanager.services.auth.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
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

    // 3. Gửi OTP
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam("username") String username) {
        try {
            authService.sendOtp(username);
            return ResponseEntity.ok("Mã OTP đã được gửi!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Xác thực OTP
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

    // =========================================================
    // --- BỔ SUNG CÁC API CÒN THIẾU (QUAN TRỌNG) ---
    // =========================================================

    // 5. Quên mật khẩu (API này đang bị thiếu gây lỗi 404)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            authService.forgotPassword(email);
            return ResponseEntity
                    .ok(Map.of("success", true, "message", "Mật khẩu mới đã được gửi vào email: " + email));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 6. Google Login (Nhận token từ Frontend gửi về)
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        try {
            // Frontend sẽ gửi JSON: { "token": "..." }
            String token = body.get("token");
            return ResponseEntity.ok(authService.loginWithGoogle(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 7. Thay đổi mật khẩu
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Collections.singletonMap("message", "Chưa đăng nhập"));
        }

        try {
            authService.changePassword(principal.getName(), request);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Đổi mật khẩu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }
}