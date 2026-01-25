package com.example.buildingmanager.models.admin.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
public class BuildingSearchResponse implements Serializable {
    private Long id;
    private String name;

    // Địa chỉ đã gộp: "59 Phan Xích Long, Phường 2, Quận Phú Nhuận"
    private String address;
    private String transactionType; // Giá trị: "RENT" hoặc "SALE"
    private Integer numberOfBasement;
    private String managerName;
    private String managerPhone;
    private Integer floorArea;
    private Integer rentPrice;
    private String serviceFee;

    // Chuỗi diện tích trống: "100, 200, 300" (Lấy từ bảng rentarea để hiển thị)
    private String emptyArea;

    private String avatar; // Ảnh thumbnail
    private String brokerageFee; // Admin cần xem phí môi giới
}
