package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "assignmentcustomer")
// Bảng phân công khách hàng cho nhân viên
public class AssignmentCustomer {
    // Class theo PascalCase

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staffid", nullable = false)
    private User staff;
    // Nhân viên được phân công

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customerid", nullable = false)
    private Customer customer;
    // Khách hàng được giao

    @Column(name = "createddate")
    private Instant createdDate;  // Thời điểm tạo

    @Column(name = "modifieddate")
    private Instant modifiedDate; // Thời điểm chỉnh sửa

    @Column(name = "createdby")
    private String createdBy;     // Người tạo

    @Column(name = "modifiedby")
    private String modifiedBy;    // Người sửa
}
