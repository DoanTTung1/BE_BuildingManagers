package com.example.buildingmanager.services.building;

import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse;
import com.example.buildingmanager.models.users.BuildingSearchDTO;

import java.util.List;

public interface IBuildingService {
    // --- KHÁCH HÀNG (Public) ---
    BuildingDetailResponse getBuildingById(Long id);
    List<BuildingSearchResponse> findAll(BuildingSearchDTO searchDTO);

    // --- ADMIN (Quản trị) ---
    // 1. Tìm kiếm (Dùng Builder của Admin)
    List<BuildingSearchResponse> findAll(BuildingSearchBuilder builder);

    // 2. Lấy chi tiết (Trả về SearchResponse cho Admin form)
    BuildingSearchResponse findById(Long id);

    // 3. Thêm mới
    UpdateAndCreateBuildingDTO createBuilding(UpdateAndCreateBuildingDTO dto);

    // 4. Cập nhật
    UpdateAndCreateBuildingDTO updateBuilding(UpdateAndCreateBuildingDTO dto);

    // 5. Xóa mềm (Soft Delete)
    void softDeleteBuilding(Long id);

    // 6. Xóa cứng (Hard Delete)
    void hardDeleteBuilding(Long id);
}