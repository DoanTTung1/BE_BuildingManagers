package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "renttype")
public class Renttype {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code; // VD: NOI_THAT

    @Column(name = "name", nullable = false)
    private String name; // VD: Nội thất

    // Link ngược lại Building (Optional - có thể bỏ nếu không cần)
    @ManyToMany(mappedBy = "rentTypes", fetch = FetchType.LAZY)
    private List<Building> buildings = new ArrayList<>();
}