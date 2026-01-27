package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "building")
@EntityListeners(AuditingEntityListener.class)
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "street")
    private String street;

    @Column(name = "ward")
    private String ward;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "districtid", nullable = false)
    private District district;

    @Column(name = "structure")
    private String structure;

    @Column(name = "numberofbasement")
    private Integer numberOfBasement;

    @Column(name = "floorarea")
    private Integer floorArea;

    @Column(name = "direction")
    private String direction;

    @Column(name = "level")
    private String level;

    @Column(name = "rentprice", nullable = false)
    private Long rentPrice;

    @Lob
    @Column(name = "rentpricedescription", columnDefinition = "TEXT")
    private String rentPriceDescription;

    @Column(name = "servicefee")
    private String serviceFee;

    @Column(name = "carfee")
    private String carFee;

    @Column(name = "motorbikefee")
    private String motorbikeFee;

    @Column(name = "overtimefee")
    private String overtimeFee;

    @Column(name = "waterfee")
    private String waterFee;

    @Column(name = "electricityfee")
    private String electricityFee;

    @Column(name = "deposit")
    private String deposit;

    @Column(name = "payment")
    private String payment;

    @Column(name = "renttime")
    private String rentTime;

    @Column(name = "decorationtime")
    private String decorationTime;

    @Column(name = "brokeragefee", precision = 13, scale = 2)
    private BigDecimal brokerageFee;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "linkofbuilding")
    private String linkOfBuilding;

    @Column(name = "map")
    private String map;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "status")
    private Integer status;

    // Thêm cột type cho khớp DB
    @Column(name = "type")
    private String type;

    @Column(name = "transaction_type")
    private String transactionType;

    // --- AUDITING ---
    @CreatedDate
    @Column(name = "createddate", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modifieddate")
    private LocalDateTime modifiedDate;

    @Column(name = "createdby")
    private String createdBy;

    @Column(name = "modifiedby")
    private String modifiedBy;

    @Column(name = "managername")
    private String managerName;

    @Column(name = "managerphonenumber")
    private String managerPhoneNumber;

    // ================= RELATIONSHIPS =================

    // 1. Rent Types (Chuẩn)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "buildingrenttype", joinColumns = @JoinColumn(name = "buildingid"), inverseJoinColumns = @JoinColumn(name = "renttypeid"))
    private List<Renttype> rentTypes = new ArrayList<>();

    // 2. Staffs Assignment (Sửa lại thành ManyToMany để dễ code giao việc)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "assignmentbuilding", joinColumns = @JoinColumn(name = "buildingid"), inverseJoinColumns = @JoinColumn(name = "staffid"))
    private List<User> staffs = new ArrayList<>();

    // 3. Rent Areas
    @OneToMany(mappedBy = "building", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Rentarea> rentAreas = new ArrayList<>();

    // 4. Building Images
    @OneToMany(mappedBy = "building", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BuildingImage> buildingImages = new ArrayList<>();
}