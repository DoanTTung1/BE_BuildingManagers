package com.example.buildingmanager.repositories;

import com.example.buildingmanager.entities.BuildingImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingImageRepository extends JpaRepository<BuildingImage, Long> {
    // Hàm xóa tất cả ảnh của 1 tòa nhà (Dùng khi update tòa nhà)
    void deleteByBuilding_Id(Long buildingId);
}