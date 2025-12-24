package com.example.buildingmanager.repositories;

import com.example.buildingmanager.entities.Rentarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RentAreaRepository extends JpaRepository<Rentarea,Long> {
    @Modifying
    @Query("DELETE FROM Rentarea r WHERE r.building.id = :buildingId") // Hoặc dùng Derived Query
    void deleteByBuildingId(Long buildingId);}
