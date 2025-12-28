package com.example.buildingmanager.services.building;

import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse;
import com.example.buildingmanager.models.users.BuildingSearchDTO; // Import DTO tìm kiếm
import java.util.List;

public interface IBuildingService {
    // Hàm xem chi tiết (đã làm lúc nãy)
    BuildingDetailResponse getBuildingById(Long id);

    // --- THÊM HÀM NÀY ---
    // Hàm tìm kiếm (Search) dùng Specification
    List<BuildingSearchResponse> findAll(BuildingSearchDTO searchDTO);
}