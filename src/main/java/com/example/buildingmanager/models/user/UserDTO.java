package com.example.buildingmanager.models.user;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String userName;
    private String fullName;
    private String phone;
    private String email;
    private Integer status;
    
    // Sửa thành List để hứng được nhiều quyền (VD: ["ADMIN", "STAFF"])
    private List<String> roleCodes = new ArrayList<>(); 
}