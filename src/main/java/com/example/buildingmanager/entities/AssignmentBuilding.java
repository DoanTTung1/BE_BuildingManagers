package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "assignmentbuilding")
// Bảng phân công tòa nhà cho nhân viên
public class AssignmentBuilding {


    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staffid", nullable = false)
    private User staff;
    // Nhân viên được giao tòa nhà

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buildingid", nullable = false)
    private Building building;
    // Tòa nhà được giao

    @Column(name = "createddate")
    private Instant createdDate;  // Thời điểm tạo

    @Column(name = "modifieddate")
    private Instant modifiedDate; // Thời điểm chỉnh sửa

    @Column(name = "createdby")
    private String createdBy;     // Người tạo

    @Column(name = "modifiedby")
    private String modifiedBy;    // Người sửa
}
