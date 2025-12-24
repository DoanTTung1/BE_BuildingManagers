package com.example.buildingmanager.models.admin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
@Builder
@Getter
@Setter
public class UpdateAndCreateBuildingDTO {

    private Long id;            // Null nếu thêm mới, Có giá trị nếu sửa
    private String name;        // Tên tòa nhà
    private String street;      // Tên đường
    private String ward;        // Phường
    private Long districtId;    // ID Quận

    private String structure;          // Kết cấu
    private Integer numberOfBasement;  // Số tầng hầm
    private Integer floorArea;         // Diện tích sàn
    private String direction;          // Hướng
    private String level;              // Hạng

    private Integer rentPrice;             // Giá thuê
    private String rentPriceDescription;   // Mô tả giá
    private String serviceFee;             // Phí dịch vụ
    private String carFee;                 // Phí ô tô
    private String motorbikeFee;           // Phí xe máy
    private String overtimeFee;            // Phí ngoài giờ
    private String waterFee;               // Phí nước
    private String electricityFee;         // Phí điện
    private String deposit;                // Đặt cọc
    private String payment;                // Thanh toán
    private String rentTime;               // Thời hạn thuê
    private String decorationTime;         // Thời gian trang trí
    private BigDecimal brokerageFee; // Phí môi giới
    private String note;            // Ghi chú
    private String linkOfBuilding;  // Link website
    private String map;             // Link bản đồ
    private String avatar;// Tên ảnh đại diện
    private String image;// Hình ảnh tòa nhà
    private String managerName;         // Tên quản lý
    private String managerPhoneNumber;  // SĐT quản lý

    // --- CÁC FIELD ĐẶC BIỆT (Xử lý logic riêng) ---

    // Input: "100, 200, 300" -> Service sẽ cắt chuỗi lưu vào bảng RentArea
    private String rentArea;

    // Input: Checkbox ["TANG_TRET", "NOI_THAT"] -> Service lưu vào bảng trung gian
    private List<String> typeCode;
}
