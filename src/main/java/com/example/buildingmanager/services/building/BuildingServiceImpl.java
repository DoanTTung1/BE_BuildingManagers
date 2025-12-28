package com.example.buildingmanager.services.building;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.mapper.BuildingConverter;
import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse;
import com.example.buildingmanager.models.users.BuildingSearchDTO;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.specifications.BuildingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // Đảm bảo toàn vẹn dữ liệu
public class BuildingServiceImpl implements IBuildingService {

    private final BuildingRepository buildingRepository;
    private final BuildingConverter buildingConverter;

    // --- PHẦN KHÁCH HÀNG (PUBLIC) ---

    @Override
    public BuildingDetailResponse getBuildingById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà ID: " + id));
        // Kiểm tra nếu tòa nhà bị khóa/xóa mềm thì không cho khách xem (Optional)
        /* if (building.getStatus() == 0) {
             throw new RuntimeException("Tòa nhà này hiện không khả dụng");
        } */
        return buildingConverter.toDetailResponse(building);
    }

    @Override
    public List<BuildingSearchResponse> findAll(BuildingSearchDTO searchDTO) {
        // 1. Tạo điều kiện tìm kiếm từ DTO (User)
        Specification<Building> spec = BuildingSpecification.build(searchDTO);
        
        // 2. Gọi Repository tìm theo điều kiện (Chứ không findAll bừa bãi nữa)
        List<Building> buildings = buildingRepository.findAll(spec);
        
        // 3. Convert sang Response
        return buildings.stream()
                .map(buildingConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    // --- PHẦN QUẢN TRỊ VIÊN (ADMIN) ---

    @Override
    public List<BuildingSearchResponse> findAll(BuildingSearchBuilder builder) {
        // 1. Tạo điều kiện tìm kiếm từ Builder (Admin)
        Specification<Building> spec = BuildingSpecification.build(builder);
        
        // 2. Tìm kiếm
        List<Building> buildings = buildingRepository.findAll(spec);
        
        // 3. Convert
        return buildings.stream()
                .map(buildingConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BuildingSearchResponse findById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà ID: " + id));
        return buildingConverter.toResponseDTO(building);
    }

    @Override
    public UpdateAndCreateBuildingDTO createBuilding(UpdateAndCreateBuildingDTO dto) {
        // 1. Convert DTO -> Entity
        Building building = buildingConverter.toEntity(dto);
        
        // Mặc định khi tạo mới thì status = 1 (Active)
        building.setStatus(1);

        // 2. Lưu xuống DB
        Building savedBuilding = buildingRepository.save(building);
        
        // 3. Trả về DTO
        return buildingConverter.toDTO(savedBuilding);
    }

    @Override
    public UpdateAndCreateBuildingDTO updateBuilding(UpdateAndCreateBuildingDTO dto) {
        // 1. Tìm cái cũ
        Building building = buildingRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà để sửa!"));

        // 2. Update thông tin mới vào cái cũ
        buildingConverter.updateEntity(dto, building);

        // 3. Lưu lại
        Building savedBuilding = buildingRepository.save(building);
        return buildingConverter.toDTO(savedBuilding);
    }

    @Override
    public void softDeleteBuilding(Long id) {
        // Xóa mềm: Chỉ đổi status về 0
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà để xóa!"));
        
        building.setStatus(0); // 0 = Đã xóa / Ngừng hoạt động
        buildingRepository.save(building);
    }

    @Override
    public void hardDeleteBuilding(Long id) {
        // Xóa vĩnh viễn khỏi Database
        if (buildingRepository.existsById(id)) {
            buildingRepository.deleteById(id);
        } else {
             throw new RuntimeException("Không tìm thấy tòa nhà để xóa vĩnh viễn!");
        }
    }
}