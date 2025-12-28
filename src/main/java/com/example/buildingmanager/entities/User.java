package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date; // Hoặc dùng java.time.LocalDateTime
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

    // --- Bổ sung các trường còn thiếu theo SQL ---
    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "status", nullable = false)
    private Integer status;

    // --- Các trường Audit (Ngày tạo, người tạo) ---
    @Column(name = "createddate")
    private Date createdDate;

    @Column(name = "modifieddate")
    private Date modifiedDate;

    @Column(name = "createdby")
    private String createdBy;

    @Column(name = "modifiedby")
    private String modifiedBy;

    // --- Quan hệ Many-to-Many với Role (thông qua bảng user_role) ---
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", 
            joinColumns = @JoinColumn(name = "userid"), 
            inverseJoinColumns = @JoinColumn(name = "roleid")) 
    private Set<Role> roles = new HashSet<>();
}