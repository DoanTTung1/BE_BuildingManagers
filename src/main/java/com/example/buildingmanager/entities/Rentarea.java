package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rentarea") // Tên bảng trong DB estatebasic
public class Rentarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value")
    private Integer value;

    // --- ĐÂY LÀ CHỖ QUAN TRỌNG ĐỂ HẾT LỖI BÊN BUILDING ---
    // Biến này tên là "building", nên bên kia mới mappedBy = "building" được
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buildingid", nullable = false) // Tên cột khóa ngoại trong DB
    private Building building;

    @Column(name = "createddate")
    private java.time.LocalDateTime createdDate;

    @Column(name = "modifieddate")
    private java.time.LocalDateTime modifiedDate;

    @Column(name = "createdby")
    private String createdBy;

    @Column(name = "modifiedby")
    private String modifiedBy;

    // Cột status bạn có trong DB
    @Column(name = "status")
    private String status;
}