package com.example.buildingmanager.models.contact;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactDTO {
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private String status;
}