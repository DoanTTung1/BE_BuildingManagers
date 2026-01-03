package com.example.buildingmanager.services.building;

import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse;
import com.example.buildingmanager.models.user.BuildingSearchDTO;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBuildingService {
    // --- KHÁCH HÀNG (Public) ---
    BuildingDetailResponse getBuildingById(Long id);

    Page<BuildingSearchResponse> findAll(BuildingSearchDTO searchDTO, Pageable pageable);

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

    // Giao tòa nhà (id) cho danh sách nhân viên (staffIds)
    void assignBuildingToStaffs(Long buildingId, List<Long> staffIds);

    List<BuildingSearchResponse> getMyBuildings(String username);
}