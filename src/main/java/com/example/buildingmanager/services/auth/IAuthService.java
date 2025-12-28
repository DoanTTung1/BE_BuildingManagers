package com.example.buildingmanager.services.auth;

import com.example.buildingmanager.models.auth.AuthResponse;
import com.example.buildingmanager.models.auth.LoginRequest;
import com.example.buildingmanager.models.auth.RegisterRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest request);
    String register(RegisterRequest request);
}