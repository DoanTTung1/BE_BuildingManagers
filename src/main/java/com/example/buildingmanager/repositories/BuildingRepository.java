package com.example.buildingmanager.repositories;

import com.example.buildingmanager.entities.Building;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BuildingRepository extends JpaRepository<Building,Long> , JpaSpecificationExecutor<Building> {
    List<Building> findByCreatedBy(String createdBy);
}
