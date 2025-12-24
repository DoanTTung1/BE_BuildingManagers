package com.example.buildingmanager.controllers.admin;

import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.services.admin.building.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/building") // Sửa lại chút để URL rõ ràng hơn: /api/admin/building
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới được gọi các API này
public class BuildingAPI {

    // Inject Service (bắt buộc phải có final để @RequiredArgsConstructor hoạt động)
    private final BuildingService buildingService;

    /**
     * 1. Tìm kiếm tòa nhà
     * URL: GET /api/admin/building?name=Landmark&floorArea=100...
     */
    @GetMapping
    public ResponseEntity<List<BuildingSearchResponse>> findBuildings(@ModelAttribute BuildingSearchBuilder builder) {
        List<BuildingSearchResponse> result = buildingService.findAll(builder);
        return ResponseEntity.ok(result);
    }

    /**
     * 2. Xem chi tiết tòa nhà theo ID
     * URL: GET /api/admin/building/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BuildingSearchResponse> getBuildingById(@PathVariable Long id) {
        BuildingSearchResponse result = buildingService.findById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 3. Thêm mới tòa nhà
     * URL: POST /api/admin/building
     */
    @PostMapping
    public ResponseEntity<UpdateAndCreateBuildingDTO> createBuilding(@RequestBody UpdateAndCreateBuildingDTO dto) {
        UpdateAndCreateBuildingDTO result = buildingService.createBuilding(dto);
        // Trả về code 201 (Created) chuẩn REST
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * 4. Cập nhật tòa nhà
     * URL: PUT /api/admin/building
     */
    @PutMapping
    public ResponseEntity<UpdateAndCreateBuildingDTO> updateBuilding(@RequestBody UpdateAndCreateBuildingDTO dto) {
        UpdateAndCreateBuildingDTO result = buildingService.updateBuilding(dto);
        return ResponseEntity.ok(result);
    }

    /**
     * 5. Xóa tòa nhà (Xóa mềm - Soft Delete)
     * URL: DELETE /api/admin/building/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBuilding(@PathVariable Long id) {
        buildingService.softDeleteBuilding(id);
        return ResponseEntity.ok("Đã xóa tòa nhà (Soft Delete) thành công!");
    }

    /**
     * 6. Xóa vĩnh viễn (Hard Delete) - API phụ
     * URL: DELETE /api/admin/building/hard/{id}
     */
    @DeleteMapping("/hard/{id}")
    public ResponseEntity<String> hardDeleteBuilding(@PathVariable Long id) {
        buildingService.hardDeleteBuilding(id);
        return ResponseEntity.ok("Đã xóa vĩnh viễn dữ liệu tòa nhà!");
    }
}