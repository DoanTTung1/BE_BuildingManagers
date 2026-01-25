package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.example.buildingmanager.enums.TransactionType;
import com.example.buildingmanager.enums.ConsignmentStatus;
import com.example.buildingmanager.enums.Direction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "consignment_requests", indexes = {
        @Index(name = "idx_status", columnList = "status"), // Tối ưu tìm kiếm theo trạng thái
        @Index(name = "idx_customer_phone", columnList = "customer_phone") // Tối ưu check trùng SĐT
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // Kích hoạt tính năng tự động ghi ngày tháng
public class ConsignmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // 1. THÔNG TIN KHÁCH HÀNG (CONTACT INFO)
    // ==========================================
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 15)
    private String customerPhone;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    // ==========================================
    // 2. THÔNG TIN TÀI SẢN (PROPERTY DETAILS)
    // ==========================================
    @Column(name = "building_name", nullable = false)
    private String buildingName; // Tên tòa nhà khách đặt (Ví dụ: Tòa nhà ABC)

    @Column(name = "address", nullable = false, length = 255)
    private String address; // Địa chỉ cụ thể (Số nhà, đường)

    // Lưu ID quận (Số) hoặc Code quận (Chữ) tùy quy ước hệ thống của bạn
    // Ở đây tôi để String để linh hoạt (VD: "1", "QUAN_1")
    @Column(name = "district_code", nullable = false, length = 50)
    private String districtCode;

    @Column(name = "ward", length = 50)
    private String ward; // Phường

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", length = 20)
    private Direction direction; // Hướng

    // ==========================================
    // 3. THÔNG SỐ KỸ THUẬT (SPECS)
    // ==========================================
    @Column(name = "number_of_floors")
    private Integer numberOfFloors; // Số tầng

    @Column(name = "floor_area")
    private Integer floorArea; // Diện tích 1 sàn (m2)

    @Column(name = "total_area")
    private Integer totalArea; // Tổng diện tích sử dụng (m2)

    // ==========================================
    // 4. THÔNG TIN TÀI CHÍNH (FINANCIAL)
    // ==========================================
    // Quan trọng: Dùng BigDecimal cho tiền tệ. precision=15, scale=2 (15 số, 2 số
    // lẻ)
    @Column(name = "expected_price", precision = 15, scale = 2)
    private BigDecimal expectedPrice;

    @Column(name = "currency", length = 10)
    private String currency = "VND";

    // ==========================================
    // 5. MÔ TẢ & GHI CHÚ
    // ==========================================
    @Lob // Large Object - Cho phép lưu văn bản dài
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Mô tả do khách nhập

    @Lob
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote; // Ghi chú nội bộ của Sale (VD: "Khách khó tính, gọi sau 5h chiều")

    // ==========================================
    // 6. QUẢN LÝ ẢNH (IMAGES)
    // ==========================================
    // Lưu danh sách URL ảnh, phân cách bằng dấu phẩy hoặc JSON string.
    // Cách này gọn nhẹ cho bảng Staging. Khi duyệt tin mới chuyển sang bảng Image
    // riêng.
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    // ==========================================
    // 7. QUẢN LÝ TRẠNG THÁI (WORKFLOW)
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConsignmentStatus status = ConsignmentStatus.PENDING; // Mặc định là Chờ xử lý

    // ==========================================
    // 8. AUDITING (TỰ ĐỘNG GHI LOG)
    // ==========================================
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType transactionType; // SELL / RENT
}