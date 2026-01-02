package com.example.buildingmanager.services.auth;

import com.example.buildingmanager.models.auth.AuthResponse;
import com.example.buildingmanager.models.auth.LoginRequest;
import com.example.buildingmanager.models.auth.RegisterRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest request);

    void sendOtp(String username);

    AuthResponse register(RegisterRequest request);

    boolean verifyOtp(String username, String otpInput);
}