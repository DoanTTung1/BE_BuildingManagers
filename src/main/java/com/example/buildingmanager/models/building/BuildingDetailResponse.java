package com.example.buildingmanager.models.building;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class BuildingDetailResponse {
    private Long id;
    private String name;
    private String address;     // Gộp: Street + Ward + District Name
    private String districtName;
    private String structure;
    private Integer numberOfBasement;
    private Integer floorArea;
    private String direction;
    private String level;
    private Integer rentPrice;
    private String rentPriceDescription;
    private String serviceFee;
    private String carFee;          // Khớp với Entity
    private String motorbikeFee;    // Khớp với Entity
    private String overtimeFee;     // Khớp với Entity
    private String waterFee;
    private String electricityFee;
    private String deposit;
    private String payment;
    private String rentTime;
    private String decorationTime;
    private BigDecimal brokerageFee;
    private String note;
    private String linkOfBuilding;
    private String map;
    private String image;           // Ảnh đại diện
    
    // Thông tin quản lý
    private String managerName;
    private String managerPhoneNumber; // Khớp với Entity

    // Diện tích thuê (VD: "100, 200, 300 m2")
    private String rentAreaResult;
}