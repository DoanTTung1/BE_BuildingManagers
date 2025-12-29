package com.example.buildingmanager.repositories;

import com.example.buildingmanager.entities.AssignmentBuilding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AssignmentBuildingRepository extends JpaRepository<AssignmentBuilding, Long> {
    
    // Hàm xóa tất cả phân công của 1 tòa nhà (Dùng để reset trước khi giao mới)
    // @Transactional và @Modifying bắt buộc phải có khi dùng delete/update custom
    @Transactional
    void deleteByBuilding_Id(Long buildingId);
}