package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "building_image")
public class BuildingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    // QUAN TRỌNG: Đặt tên biến là "link" để khớp với BuildingConverter
    @Column(name = "link", nullable = false)
    private String link; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buildingid")
    private Building building;
}