package com.example.buildingmanager.repositories;

import com.example.buildingmanager.entities.Building;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BuildingRepository extends JpaRepository<Building,Long> , JpaSpecificationExecutor<Building> {
}
