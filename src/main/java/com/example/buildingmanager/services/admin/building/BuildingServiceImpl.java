package com.example.buildingmanager.services.admin.building;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.Rentarea;
import com.example.buildingmanager.mapper.BuildingConverter;
import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.repositories.RentAreaRepository;
import com.example.buildingmanager.specifications.BuildingSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 1. Thêm Logging
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils; // Tiện ích xử lý chuỗi

import java.util.List;
import java.util.stream.Collectors;

@Slf4j // Tự động tạo biến log
@RequiredArgsConstructor
@Service
@Transactional
public class BuildingServiceImpl implements BuildingService {
    private final BuildingRepository buildingRepository;
    private final RentAreaRepository rentAreaRepository;
    private final BuildingConverter buildingConverter;

    @Override
    public List<BuildingSearchResponse> findAll(BuildingSearchBuilder builder) {
        Specification<Building> spec = BuildingSpecification.build(builder);
        List<Building> buildingEntities = buildingRepository.findAll(spec);

        // 2. Dùng Stream API cho gọn
        return buildingEntities.stream()
                .map(buildingConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UpdateAndCreateBuildingDTO createBuilding(UpdateAndCreateBuildingDTO dto) {
        // 3. Logic tạo mới an toàn hơn
        if (dto.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khi tạo mới, ID phải để trống");
        }

        Building buildingEntity = buildingConverter.toEntity(dto);
        // Đảm bảo entity mới không có ID để JPA tự sinh
        buildingEntity.setId(null);

        Building savedBuilding = buildingRepository.save(buildingEntity);
        saveRentArea(dto.getRentArea(), savedBuilding);

        return buildingConverter.toDTO(savedBuilding);
    }

    @Override
    public UpdateAndCreateBuildingDTO updateBuilding(UpdateAndCreateBuildingDTO buildingDTO) {
        Long id = buildingDTO.getId();
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID không được để trống khi cập nhật");
        }

        Building oldBuilding = buildingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy toà nhà ID: " + id));

        // 4. Cập nhật dữ liệu: Thay vì tạo entity mới, hãy update trên entity cũ
        // Lưu ý: Cần đảm bảo toEntity của bạn xử lý đúng, hoặc dùng cách map thủ công các trường cần update ở đây
        // Giả sử logic cũ của bạn là muốn replace toàn bộ trừ field hệ thống:
        Building updateInfo = buildingConverter.toEntity(buildingDTO);

        oldBuilding.setName(updateInfo.getName());
        oldBuilding.setStreet(updateInfo.getStreet());
        oldBuilding.setWard(updateInfo.getWard());
        oldBuilding.setDistrict(updateInfo.getDistrict());
        // ... map các trường khác ...

        // Giữ logic ảnh cũ nếu ảnh mới rỗng
        if (StringUtils.hasText(buildingDTO.getImage())) {
            oldBuilding.setImage(buildingDTO.getImage());
        }

        // 5. Xử lý RentArea
        rentAreaRepository.deleteByBuildingId(id);
        saveRentArea(buildingDTO.getRentArea(), oldBuilding);

        Building savedBuilding = buildingRepository.save(oldBuilding);
        return buildingConverter.toDTO(savedBuilding);
    }

    private void saveRentArea(String areaInput, Building building) {
        if (!StringUtils.hasText(areaInput)) return;

        String[] areas = areaInput.split(",");
        for (String item : areas) {
            try {
                Rentarea rentArea = new Rentarea();
                rentArea.setValue(Integer.parseInt(item.trim()));
                rentArea.setStatus("AVAILABLE"); // Nên đưa vào Constant hoặc Enum
                rentArea.setBuilding(building);
                rentAreaRepository.save(rentArea);
            } catch (NumberFormatException e) {
                // 6. Log lỗi chuẩn và có thể ném lỗi ra client nếu cần thiết
                log.error("Lỗi định dạng diện tích thuê: '{}'", item, e);
                // Tùy nghiệp vụ: Có thể throw exception để rollback transaction nếu dữ liệu sai
                // throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Diện tích không hợp lệ: " + item);
            }
        }
    }

    @Override
    public void softDeleteBuilding(Long id) {
        // Có thể dùng ifPresent để code ngắn hơn nếu không cần trả về
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tòa nhà ID: " + id));
        building.setStatus(0); // Nên dùng Enum hoặc Constant thay vì số 0 cứng
        buildingRepository.save(building);
    }

    @Override
    public void hardDeleteBuilding(Long id) {
        if (!buildingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tòa nhà ID: " + id);
        }
        // Lưu ý: Nếu trong Entity Building bạn cấu hình CascadeType.REMOVE cho list RentArea
        // thì không cần gọi dòng deleteByBuildingId dưới đây.
        rentAreaRepository.deleteByBuildingId(id);
        buildingRepository.deleteById(id);
    }

    @Override
    public BuildingSearchResponse findById(Long id) {
        return buildingRepository.findById(id)
                .map(buildingConverter::toResponseDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tòa nhà ID: " + id));
    }
}