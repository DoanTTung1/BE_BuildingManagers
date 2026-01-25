package com.example.buildingmanager.models.admin.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffResponseDTO {
    private Long id;
    private String fullName;
    private String checked; // "checked" nếu nhân viên đó đang quản lý tòa nhà, ngược lại là ""
}