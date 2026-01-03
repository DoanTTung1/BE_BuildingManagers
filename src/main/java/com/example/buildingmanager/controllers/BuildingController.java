package com.example.buildingmanager.controllers;

import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse; // DTO chi tiết
import com.example.buildingmanager.models.user.BuildingSearchDTO; // DTO tìm kiếm của User
import com.example.buildingmanager.services.building.IBuildingService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600) // Cho phép FE gọi
public class BuildingController {

    private final IBuildingService buildingService;

    // ==================== PHẦN PUBLIC (Ai cũng dùng được) ====================

    /**
     * 1. Tìm kiếm tòa nhà
     * Dùng BuildingSearchDTO để hỗ trợ tìm theo Quận, Giá, Diện tích...
     */
    @GetMapping
    public ResponseEntity<Page<BuildingSearchResponse>> searchBuildingsPublic(
            @ModelAttribute BuildingSearchDTO searchDTO,
            // page = số trang (bắt đầu từ 0), size = số lượng item/trang
            @PageableDefault(size = 6) Pageable pageable) {
        Page<BuildingSearchResponse> result = buildingService.findAll(searchDTO, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<BuildingSearchResponse>> searchBuildingsAdmin(
            @ModelAttribute BuildingSearchBuilder builder) {
        // Lưu ý: Bạn cần import class BuildingSearchBuilder
        List<BuildingSearchResponse> result = buildingService.findAll(builder);
        return ResponseEntity.ok(result);
    }

    /**
     * 2. Xem chi tiết tòa nhà
     */
    @GetMapping("/{id}")
    public ResponseEntity<BuildingDetailResponse> getBuildingById(@PathVariable Long id) {
        BuildingDetailResponse result = buildingService.getBuildingById(id);
        return ResponseEntity.ok(result);
    }

    // ==================== PHẦN CÓ PHÂN QUYỀN (Login mới dùng được)
    // ====================

    /**
     * 3. Đăng tin / Thêm mới
     * - SecurityConfig đã cho phép: USER, STAFF, ADMIN
     */
    @PostMapping
    public ResponseEntity<UpdateAndCreateBuildingDTO> createBuilding(@RequestBody UpdateAndCreateBuildingDTO dto) {
        UpdateAndCreateBuildingDTO result = buildingService.createBuilding(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * 4. Cập nhật thông tin
     * - SecurityConfig đã chặn: Chỉ STAFF, ADMIN
     */
    @PutMapping
    public ResponseEntity<UpdateAndCreateBuildingDTO> updateBuilding(@RequestBody UpdateAndCreateBuildingDTO dto) {
        UpdateAndCreateBuildingDTO result = buildingService.updateBuilding(dto);
        return ResponseEntity.ok(result);
    }

    /**
     * 5. Xóa mềm (Soft Delete)
     * - @PreAuthorize: Chỉ ADMIN mới được xóa
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteBuilding(@PathVariable Long id) {
        buildingService.softDeleteBuilding(id);
        return ResponseEntity.ok("Đã xóa tòa nhà thành công!");
    }

    /**
     * 6. Xóa vĩnh viễn (Hard Delete)
     * - @PreAuthorize: Chỉ ADMIN
     */
    @DeleteMapping("/hard/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> hardDeleteBuilding(@PathVariable Long id) {
        buildingService.hardDeleteBuilding(id);
        return ResponseEntity.ok("Đã xóa vĩnh viễn dữ liệu!");
    }

    @PostMapping("/{id}/assignment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignBuilding(@PathVariable Long id, @RequestBody List<Long> staffIds) {
        buildingService.assignBuildingToStaffs(id, staffIds);
        return ResponseEntity.ok("Giao tòa nhà thành công!");
    }

    @GetMapping("/my-posts")
    public ResponseEntity<?> getMyBuildings() {
        // Lấy username của người đang đăng nhập
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        List<BuildingSearchResponse> result = buildingService.getMyBuildings(currentUsername);
        return ResponseEntity.ok(result);
    }
}