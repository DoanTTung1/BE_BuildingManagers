package com.example.buildingmanager.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    // Mapping ngược lại User (Optional - không bắt buộc nếu không cần lấy list user từ role)
    @ManyToMany(mappedBy = "roles")
    private List<User> users = new ArrayList<>();
}