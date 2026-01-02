package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.time.LocalDateTime; // Import cái này để xử lý thời gian hết hạn OTP chính xác hơn Date
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "status", nullable = false)
    private Integer status;

    // --- CÁC TRƯỜNG MỚI BỔ SUNG CHO TÍNH NĂNG OTP ---

    @Column(name = "is_phone_verified")
    private boolean phoneVerified = false; // Mặc định false (chưa xác thực)

    @Column(name = "otp_code")
    private String otpCode; // Lưu mã OTP 6 số

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry; // Thời gian hết hạn của mã OTP

    // ------------------------------------------------

    // --- Các trường Audit ---
    @Column(name = "createddate")
    private Date createdDate;

    @Column(name = "modifieddate")
    private Date modifiedDate;

    @Column(name = "createdby")
    private String createdBy;

    @Column(name = "modifiedby")
    private String modifiedBy;

    // --- Quan hệ Many-to-Many với Role ---
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = @JoinColumn(name = "roleid"))
    private Set<Role> roles = new HashSet<>();
}