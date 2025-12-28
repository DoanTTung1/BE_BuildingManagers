package com.example.buildingmanager.controllers.building;

import com.example.buildingmanager.models.admin.response.BuildingSearchResponse; // DTO trả về danh sách
import com.example.buildingmanager.models.building.BuildingDetailResponse; // DTO chi tiết
import com.example.buildingmanager.models.users.BuildingSearchDTO; // DTO nhận tham số tìm kiếm
import com.example.buildingmanager.services.building.IBuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buildings") // Dùng số nhiều cho chuẩn
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600) // Cho phép FE gọi
public class BuildingController {

    private final IBuildingService buildingService;

    // 1. API Tìm kiếm tòa nhà (Theo tên, quận, giá...)
    // URL: GET http://localhost:8080/api/buildings?name=ABC&districtId=1
    @GetMapping
    public ResponseEntity<List<BuildingSearchResponse>> searchBuildings(@ModelAttribute BuildingSearchDTO searchDTO) {
        List<BuildingSearchResponse> result = buildingService.findAll(searchDTO);
        return ResponseEntity.ok(result);
    }

    // 2. API Xem chi tiết tòa nhà
    // URL: GET http://localhost:8080/api/buildings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BuildingDetailResponse> getBuildingById(@PathVariable Long id) {
        BuildingDetailResponse result = buildingService.getBuildingById(id);
        return ResponseEntity.ok(result);
    }
}