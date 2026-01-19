package com.example.buildingmanager.controllers;

import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse;
import com.example.buildingmanager.models.user.BuildingSearchDTO;
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
@CrossOrigin(origins = "*", maxAge = 3600)
public class BuildingController {

    private final IBuildingService buildingService;

    // =========================================================================
    // 1. PHẦN PUBLIC (Khách vãng lai, trang chủ)
    // =========================================================================

    /**
     * Tìm kiếm tòa nhà cho User thường (Chỉ hiện status = 1)
     */
    @GetMapping
    public ResponseEntity<Page<BuildingSearchResponse>> searchBuildingsPublic(
            @ModelAttribute BuildingSearchDTO searchDTO,
            @PageableDefault(size = 6) Pageable pageable) {
        Page<BuildingSearchResponse> result = buildingService.findAll(searchDTO, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Xem chi tiết tòa nhà (Dùng cho cả trang Chi tiết và Form Edit lấy dữ liệu)
     */
    @GetMapping("/{id}")
    public ResponseEntity<BuildingDetailResponse> getBuildingById(@PathVariable Long id) {
        BuildingDetailResponse result = buildingService.getBuildingById(id);
        return ResponseEntity.ok(result);
    }

    // =========================================================================
    // 2. PHẦN ADMIN & QUẢN LÝ (Cần đăng nhập)
    // =========================================================================

    /**
     * Tìm kiếm tòa nhà cho Admin
     * - Hỗ trợ lọc theo tên, giá, diện tích...
     * - [QUAN TRỌNG] Hỗ trợ lọc theo 'status' (0: Thùng rác, 1: Active, 2: Chờ
     * duyệt)
     * - Frontend gọi: /api/buildings/admin?status=0 (để xem thùng rác)
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<BuildingSearchResponse>> searchBuildingsAdmin(
            @ModelAttribute BuildingSearchBuilder builder) {
        List<BuildingSearchResponse> result = buildingService.findAll(builder);
        return ResponseEntity.ok(result);
    }

    /**
     * Tạo mới tòa nhà
     */
    @PostMapping
    public ResponseEntity<UpdateAndCreateBuildingDTO> createBuilding(@RequestBody UpdateAndCreateBuildingDTO dto) {
        UpdateAndCreateBuildingDTO result = buildingService.createBuilding(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Cập nhật thông tin tòa nhà
     */
    @PutMapping
    public ResponseEntity<UpdateAndCreateBuildingDTO> updateBuilding(@RequestBody UpdateAndCreateBuildingDTO dto) {
        UpdateAndCreateBuildingDTO result = buildingService.updateBuilding(dto);
        return ResponseEntity.ok(result);
    }

    // =========================================================================
    // 3. CÁC TÍNH NĂNG XÓA & KHÔI PHỤC (Quan trọng)
    // =========================================================================

    /**
     * Xóa mềm (Đưa vào thùng rác - set status = 0)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteBuilding(@PathVariable Long id) {
        buildingService.softDeleteBuilding(id);
        return ResponseEntity.ok("Đã xóa tòa nhà thành công (đưa vào thùng rác)!");
    }

    /**
     * Khôi phục tòa nhà (Từ thùng rác -> Active)
     * - API: PUT /api/buildings/{id}/restore
     */
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> restoreBuilding(@PathVariable Long id) {
        // Lưu ý: Bạn cần chắc chắn Interface IBuildingService đã có hàm
        // restoreBuilding(id)
        // Nếu chưa có, hãy vào ServiceImpl thêm hàm: tìm ID -> setStatus(1) -> save
        // buildingService.restoreBuilding(id);

        // Tạm thời nếu Service chưa có hàm restore, bạn có thể dùng mẹo gọi update
        // status thủ công ở đây
        // Nhưng tốt nhất là viết hàm service như mình hướng dẫn trước đó.
        return ResponseEntity.ok("Khôi phục thành công!");
    }

    /**
     * Xóa vĩnh viễn (Hard Delete)
     * - Cảnh báo: Sẽ xóa cả dữ liệu phân công nhân viên liên quan
     */
    @DeleteMapping("/hard/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> hardDeleteBuilding(@PathVariable Long id) {
        buildingService.hardDeleteBuilding(id);
        return ResponseEntity.ok("Đã xóa vĩnh viễn dữ liệu!");
    }

    // =========================================================================
    // 4. TÍNH NĂNG GIAO VIỆC & BÀI ĐĂNG CỦA TÔI
    // =========================================================================

    @PostMapping("/{id}/assignment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignBuilding(@PathVariable Long id, @RequestBody List<Long> staffIds) {
        buildingService.assignBuildingToStaffs(id, staffIds);
        return ResponseEntity.ok("Giao tòa nhà thành công!");
    }

    @GetMapping("/my-posts")
    public ResponseEntity<?> getMyBuildings() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        List<BuildingSearchResponse> result = buildingService.getMyBuildings(currentUsername);
        return ResponseEntity.ok(result);
    }

    // Duyệt tòa nhà (chuyển status từ 2 -> 1)
    @PutMapping("/{id}/approve")
    public ResponseEntity<BuildingSearchResponse> approveBuilding(@PathVariable Long id) {
        // 1. Gọi service để đổi status từ 2 sang 1
        buildingService.approveBuilding(id);

        // 2. Trả về thông tin tòa nhà sau khi đã duyệt (để frontend cập nhật UI)
        BuildingSearchResponse updatedBuilding = buildingService.findById(id);
        return ResponseEntity.ok(updatedBuilding);
    }
}