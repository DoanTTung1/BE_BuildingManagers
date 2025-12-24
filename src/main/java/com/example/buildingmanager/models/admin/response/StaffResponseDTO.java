package com.example.buildingmanager.models.admin.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffResponseDTO {
    private Long id;
    private String fullName;

    /**
     * Trạng thái checked trong checkbox.
     * Value: "checked" (nếu nhân viên đang quản lý tòa này) hoặc "" (rỗng).
     */
    private String checked;
}