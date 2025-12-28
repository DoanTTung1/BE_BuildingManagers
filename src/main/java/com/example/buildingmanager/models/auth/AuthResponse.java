package com.example.buildingmanager.models.auth;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";

    public AuthResponse(String token) {
        this.token = token;
    }
}