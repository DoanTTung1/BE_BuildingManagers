package com.example.buildingmanager.models.auth;
import lombok.Data;

@Data
public class RegisterRequest {
    private String userName;
    private String password;
    private String fullName;
    private String email;
    private String phone;
}