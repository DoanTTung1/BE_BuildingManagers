package com.example.buildingmanager.models.admin;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor // Bổ sung để Jackson có thể tạo object trống
@AllArgsConstructor // Bổ sung để Builder hoạt động đúng
public class UpdateAndCreateBuildingDTO {

    private Long id;
    private String name;
    private String street;
    private String ward;
    private Long districtId;

    private String structure;
    private Integer numberOfBasement;
    private Integer floorArea;
    private String direction;
    private String level;

    private Integer rentPrice;
    private String rentPriceDescription;
    private String serviceFee;
    private String carFee;
    private String motorbikeFee;
    private String overtimeFee;
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

    // Thống nhất dùng image để khớp với JSON server trả về
    private String image;

    private String managerName;
    private String managerPhoneNumber;

    private String rentArea;
    private List<String> typeCode;
    private List<String> imageList;
}