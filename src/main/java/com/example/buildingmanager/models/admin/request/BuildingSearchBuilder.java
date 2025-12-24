package com.example.buildingmanager.models.admin.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BuildingSearchBuilder {
    // --- Tiêu chí tìm kiếm cơ bản ---
    private String name;
    private Integer floorArea;
    private Long districtId;
    private String ward;
    private String street;
    private Integer numberOfBasement;
    private String direction;
    private String level;
    private String managerName;
    private String managerPhone;

    // --- Tiêu chí tìm kiếm theo khoảng (Range) ---
    private Integer rentPriceFrom;
    private Integer rentPriceTo;
    private Integer areaFrom;
    private Integer areaTo;

    // --- Tiêu chí liên kết ---
    private Long staffId;           // Tìm theo nhân viên phụ trách
    private List<String> typeCode;  // Tìm theo loại tòa nhà (Checkbox)

    // --- Tiêu chí Admin ---
    // status = null (tìm hết), status = 1 (tìm đang active)
    private Integer status;
}