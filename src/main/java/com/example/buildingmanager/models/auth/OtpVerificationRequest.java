package com.example.buildingmanager.models.auth;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String userName;
    private String otp;
}