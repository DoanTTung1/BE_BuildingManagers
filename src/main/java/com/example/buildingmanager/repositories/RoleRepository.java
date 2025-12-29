package com.example.buildingmanager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.buildingmanager.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByCode(String code);
}