package com.example.buildingmanager.models.auth;
import lombok.Data;

@Data
public class LoginRequest {
    private String userName;
    private String password;
}