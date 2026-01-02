package com.example.buildingmanager.services.building;

import com.example.buildingmanager.entities.AssignmentBuilding;
import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.mapper.BuildingConverter;
import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse;
import com.example.buildingmanager.models.user.BuildingSearchDTO;
import com.example.buildingmanager.repositories.AssignmentBuildingRepository;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.repositories.UserRepository;
import com.example.buildingmanager.specifications.BuildingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // Đảm bảo toàn vẹn dữ liệu cho mọi thao tác
public class BuildingServiceImpl implements IBuildingService {

    private final BuildingRepository buildingRepository;
    private final BuildingConverter buildingConverter;
    private final UserRepository userRepository;
    private final AssignmentBuildingRepository assignmentBuildingRepository;

    // =========================================================================
    // PHẦN KHÁCH HÀNG (PUBLIC)
    // =========================================================================

    @Override
    public BuildingDetailResponse getBuildingById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà ID: " + id));

        // Convert Entity -> Detail Response
        return buildingConverter.toDetailResponse(building);
    }

    @Override
    public List<BuildingSearchResponse> findAll(BuildingSearchDTO searchDTO) {
        // 1. Tạo điều kiện tìm kiếm (Specification)
        Specification<Building> spec = BuildingSpecification.build(searchDTO);

        // 2. Query Database
        List<Building> buildings = buildingRepository.findAll(spec);

        // 3. Convert Entity -> Response
        return buildings.stream()
                .map(buildingConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // PHẦN QUẢN TRỊ VIÊN (ADMIN) & NGƯỜI ĐĂNG TIN
    // =========================================================================

    @Override
    public List<BuildingSearchResponse> findAll(BuildingSearchBuilder builder) {
        Specification<Building> spec = BuildingSpecification.build(builder);
        List<Building> buildings = buildingRepository.findAll(spec);
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

        // 2. LẤY THÔNG TIN NGƯỜI DÙNG HIỆN TẠI (QUAN TRỌNG)
        String currentUsername = "";
        try {
            // Lấy username từ Token đang đăng nhập
            currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

            // --- FIX LỖI: GÁN NGƯỜI TẠO VÀO ENTITY ---
            building.setCreatedBy(currentUsername);
            // -----------------------------------------

            // Tự động điền Tên quản lý / SĐT quản lý nếu người dùng bỏ trống
            User currentUser = userRepository.findByUserNameAndStatus(currentUsername, 1).orElse(null);
            if (currentUser != null) {
                if (building.getManagerName() == null || building.getManagerName().isEmpty()) {
                    building.setManagerName(currentUser.getFullName());
                }
                // Nếu muốn tự điền SĐT thì bỏ comment dòng dưới:
                // if (building.getManagerPhone() == null ||
                // building.getManagerPhone().isEmpty()) {
                // building.setManagerPhone(currentUser.getPhone());
                // }
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy thông tin user: " + e.getMessage());
        }

        // 3. Set trạng thái mặc định (1 = Hoạt động)
        building.setStatus(1);

        // 4. Lưu xuống Database
        Building savedBuilding = buildingRepository.save(building);

        return buildingConverter.toDTO(savedBuilding);
    }

    @Override
    public UpdateAndCreateBuildingDTO updateBuilding(UpdateAndCreateBuildingDTO dto) {
        // 1. Tìm tòa nhà cũ
        Building building = buildingRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà để sửa!"));

        // 2. Update thông tin từ DTO vào Entity cũ
        buildingConverter.updateEntity(dto, building);

        // 3. Lưu lại
        Building savedBuilding = buildingRepository.save(building);
        return buildingConverter.toDTO(savedBuilding);
    }

    @Override
    public void softDeleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà!"));

        building.setStatus(0); // 0 = Đã xóa mềm / Tạm ẩn
        buildingRepository.save(building);
    }

    @Override
    public void hardDeleteBuilding(Long id) {
        if (!buildingRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy tòa nhà!");
        }
        buildingRepository.deleteById(id);
    }

    @Override
    public void assignBuildingToStaffs(Long buildingId, List<Long> staffIds) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Tòa nhà không tồn tại!"));

        // 1. Xóa phân công cũ
        assignmentBuildingRepository.deleteByBuilding_Id(buildingId);

        // 2. Tạo phân công mới nếu có danh sách nhân viên
        if (staffIds != null && !staffIds.isEmpty()) {
            List<AssignmentBuilding> assignments = new ArrayList<>();
            for (Long staffId : staffIds) {
                User staff = userRepository.findById(staffId)
                        .orElseThrow(() -> new RuntimeException("Nhân viên ID " + staffId + " không tồn tại!"));

                AssignmentBuilding assignment = new AssignmentBuilding();
                assignment.setBuilding(building);
                assignment.setStaff(staff);
                assignments.add(assignment);
            }
            assignmentBuildingRepository.saveAll(assignments);
        }
    }

    @Override
    public List<BuildingSearchResponse> getMyBuildings(String username) {
        // 1. Tìm tòa nhà theo cột created_by trong Database
        List<Building> buildings = buildingRepository.findByCreatedBy(username);

        // 2. Convert sang Response để hiển thị ở Frontend
        return buildings.stream()
                .map(buildingConverter::toResponseDTO)
                .collect(Collectors.toList());
    }
}