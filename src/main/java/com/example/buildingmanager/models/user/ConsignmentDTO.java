package com.example.buildingmanager.models.user;


import com.example.buildingmanager.enums.Direction;
import com.example.buildingmanager.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConsignmentDTO {

    // --- Thông tin khách ---
    @NotBlank(message = "Tên khách hàng không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Pattern(regexp = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$", 
             message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @Email(message = "Email không đúng định dạng")
    private String customerEmail;

    // --- Thông tin BĐS ---
    @NotBlank(message = "Tên tòa nhà/BĐS không được để trống")
    private String buildingName;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotBlank(message = "Vui lòng chọn Quận")
    private String districtCode;

    private String ward;

    private Direction direction; // Enum

    @NotNull(message = "Loại giao dịch là bắt buộc")
    private TransactionType transactionType; // SELL hoặc RENT

    // --- Thông số ---
    @Min(value = 1, message = "Số tầng phải lớn hơn 0")
    private Integer numberOfFloors;

    @Min(value = 10, message = "Diện tích sàn tối thiểu 10m2")
    private Integer floorArea;

    private Integer totalArea;

    // --- Tài chính ---
    @NotNull(message = "Giá mong muốn không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal expectedPrice;

    private String currency = "VND";

    // --- Khác ---
    private String description;
    
    // Lưu ý: Ảnh sẽ xử lý riêng hoặc gửi kèm dạng chuỗi link nếu đã upload lên cloud
    private String imageUrls; 
}