package com.example.buildingmanager.services.auth;

import com.example.buildingmanager.models.auth.AuthResponse;
import com.example.buildingmanager.models.auth.ChangePasswordRequest;
import com.example.buildingmanager.models.auth.LoginRequest;
import com.example.buildingmanager.models.auth.RegisterRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest request);

    void sendOtp(String username);

    AuthResponse register(RegisterRequest request);

    boolean verifyOtp(String username, String otpInput);

    // 1. Hàm xử lý quên mật khẩu (Gửi email)
    void forgotPassword(String email);

    // 2. Hàm xử lý đăng nhập Google
    // Trả về AuthResponse để đồng bộ với hàm login thường
    AuthResponse loginWithGoogle(String credential) throws Exception;

    // 3. Hàm thay đổi mật khẩu
    void changePassword(String username, ChangePasswordRequest request);
}