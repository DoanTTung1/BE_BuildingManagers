package com.example.buildingmanager.repositories;

import com.example.buildingmanager.entities.AssignmentBuilding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AssignmentBuildingRepository extends JpaRepository<AssignmentBuilding, Long> {

    // ✅ CÁCH TỐI ƯU: Xóa 1 lần bằng SQL thuần (Bulk Delete)
    @Modifying
    @Transactional
    @Query("DELETE FROM AssignmentBuilding ab WHERE ab.building.id = :buildingId")
    void deleteByBuilding_Id(Long buildingId);
}