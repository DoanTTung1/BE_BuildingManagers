package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date; // Nên dùng Date để đồng bộ với User entity cũ

@Getter
@Setter
@Entity
@Table(name = "customer")
@EntityListeners(AuditingEntityListener.class) // <--- QUAN TRỌNG: Kích hoạt tính năng tự động ghi ngày giờ
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- BẮT BUỘC: Để MySQL tự tăng ID (1, 2, 3...)
    @Column(name = "id")
    private Long id;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "demand")
    private String demand;

    @Column(name = "status")
    private Integer status = 1; // Mặc định là 1 (Chờ xử lý) ngay trong Java

    // --- CÁC TRƯỜNG AUDIT (Tự động) ---

    @CreatedDate // Tự động lấy thời gian hiện tại khi tạo mới
    @Column(name = "createddate", updatable = false)
    private Date createdDate;

    @LastModifiedDate // Tự động cập nhật thời gian khi sửa
    @Column(name = "modifieddate")
    private Date modifiedDate;

    @CreatedBy // Tự động lấy username của người tạo (nếu đã cấu hình AuditorAware)
    @Column(name = "createdby")
    private String createdBy;

    @LastModifiedBy // Tự động lấy username của người sửa
    @Column(name = "modifiedby")
    private String modifiedBy;
    // --- Trường đánh dấu xóa mềm ---
    @Column(name = "is_active")
    private Integer isActive = 1;
}