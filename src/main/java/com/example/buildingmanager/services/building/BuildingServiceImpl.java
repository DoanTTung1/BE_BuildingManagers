package com.example.buildingmanager.services.building;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.mapper.BuildingConverter;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse;
import com.example.buildingmanager.models.users.BuildingSearchDTO;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.specifications.BuildingSpecification; // Đừng quên import cái này
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Dùng cái này thay cho @Autowired từng dòng (Code đẹp hơn)
public class BuildingServiceImpl implements IBuildingService {

    private final BuildingRepository buildingRepository;
    private final BuildingConverter buildingConverter;

    // 1. Hàm xem chi tiết (Code cũ giữ nguyên)
    @Override
    public BuildingDetailResponse getBuildingById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà ID: " + id));
        return buildingConverter.toDetailResponse(building);
    }

    // 2. Hàm tìm kiếm (Code bạn vừa gửi -> Dán vào đây)
    @Override
    public List<BuildingSearchResponse> findAll(BuildingSearchDTO searchDTO) {
        // Tạo điều kiện lọc từ DTO
        Specification<Building> spec = BuildingSpecification.build(searchDTO);

        // Gọi DB lấy danh sách theo điều kiện
        List<Building> buildings = buildingRepository.findAll(spec);

        // Convert sang DTO trả về
        return buildings.stream()
                .map(item -> buildingConverter.toResponseDTO(item))
                .collect(Collectors.toList());
    }
}