package com.example.buildingmanager.models.customer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerContactRequest {
    private String fullName;
    private String phone;
    private String email;
    private String demand; // Ví dụ: "Tôi muốn thuê tòa nhà A"
}