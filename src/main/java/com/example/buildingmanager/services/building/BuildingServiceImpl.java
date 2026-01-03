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

        // Optional: Chặn khách xem nếu tin chưa duyệt/đã xóa
        // if (building.getStatus() != 1) throw new RuntimeException("Tin không khả
        // dụng");

        return buildingConverter.toDetailResponse(building);
    }

    @Override
    public List<BuildingSearchResponse> findAll(BuildingSearchDTO searchDTO) {
        // [QUAN TRỌNG] Ép cứng điều kiện: Khách chỉ thấy tòa nhà ĐANG HOẠT ĐỘNG (Status
        // = 1)
        Specification<Building> spec = BuildingSpecification.build(searchDTO);

        // Dùng Specification.where để tránh NullPointerException nếu spec ban đầu null
        spec = Specification.where(spec)
                .and((root, query, cb) -> cb.equal(root.get("status"), 1));

        List<Building> buildings = buildingRepository.findAll(spec);
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

        // 2. LẤY THÔNG TIN NGƯỜI DÙNG HIỆN TẠI
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        building.setCreatedBy(currentUsername);

        // --- LOGIC XÉT DUYỆT ---
        int buildingStatus = 2; // Mặc định: CHỜ DUYỆT (User đăng)

        try {
            User currentUser = userRepository.findByUserNameAndStatus(currentUsername, 1).orElse(null);

            if (currentUser != null) {
                // Tự động điền thông tin quản lý
                if (building.getManagerName() == null || building.getManagerName().isEmpty()) {
                    building.setManagerName(currentUser.getFullName());
                }
                if (building.getManagerPhoneNumber() == null || building.getManagerPhoneNumber().isEmpty()) {
                    building.setManagerPhoneNumber(currentUser.getPhone());
                }

                // KIỂM TRA QUYỀN ADMIN
                boolean isAdmin = currentUser.getRoles().stream()
                        .anyMatch(role -> role.getCode().equals("ADMIN"));

                if (isAdmin) {
                    buildingStatus = 1; // Admin đăng -> Active luôn
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi lấy user info: " + e.getMessage());
        }

        // 3. Set trạng thái & Lưu
        building.setStatus(buildingStatus);
        Building savedBuilding = buildingRepository.save(building);

        return buildingConverter.toDTO(savedBuilding);
    }

    @Override
    public UpdateAndCreateBuildingDTO updateBuilding(UpdateAndCreateBuildingDTO dto) {
        Building building = buildingRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà!"));

        buildingConverter.updateEntity(dto, building);
        Building savedBuilding = buildingRepository.save(building);
        return buildingConverter.toDTO(savedBuilding);
    }

    @Override
    public void softDeleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà!"));
        building.setStatus(0); // 0 = Đã xóa mềm
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

        assignmentBuildingRepository.deleteByBuilding_Id(buildingId);

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
        List<Building> buildings = buildingRepository.findByCreatedBy(username);

        // [QUAN TRỌNG] Lọc bỏ các bài đã xóa mềm (status = 0)
        // User vẫn được thấy bài đang chờ duyệt (status = 2)
        return buildings.stream()
                .filter(b -> b.getStatus() != 0)
                .map(buildingConverter::toResponseDTO)
                .collect(Collectors.toList());
    }
}