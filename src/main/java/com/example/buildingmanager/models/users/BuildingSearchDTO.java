package com.example.buildingmanager.models.users;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BuildingSearchDTO {
    // Các tiêu chí user nhập vào để tìm kiếm
    private String name;          // Tên tòa nhà
    private Long floorArea;       // Diện tích sàn (ví dụ tìm diện tích lớn hơn số này)
    private String district;      // Quận
    private Long rentPriceFrom;   // Giá thuê từ...
    private Long rentPriceTo;     // ...đến giá thuê
    private String managerName;   // Tìm theo tên quản lý
    private String staffName;     // Tìm theo tên nhân viên (nếu cần)
    private List<String> typeCode; // Loại tòa nhà (Tầng trệt, Nguyên căn...)

}