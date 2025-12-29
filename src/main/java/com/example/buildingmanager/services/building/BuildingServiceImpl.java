package com.example.buildingmanager.services.building;

import com.example.buildingmanager.entities.AssignmentBuilding;
import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.mapper.BuildingConverter;
import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse;
import com.example.buildingmanager.models.users.BuildingSearchDTO;
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
@Transactional // Đảm bảo toàn vẹn dữ liệu
public class BuildingServiceImpl implements IBuildingService {

    private final BuildingRepository buildingRepository;
    private final BuildingConverter buildingConverter;
    private final UserRepository userRepository;
    private final AssignmentBuildingRepository assignmentBuildingRepository;
    
    // --- PHẦN KHÁCH HÀNG (PUBLIC) ---

    @Override
    public BuildingDetailResponse getBuildingById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà ID: " + id));
        // Kiểm tra nếu tòa nhà bị khóa/xóa mềm thì không cho khách xem (Optional)
        /*
         * if (building.getStatus() == 0) {
         * throw new RuntimeException("Tòa nhà này hiện không khả dụng");
         * }
         */
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
        // 1. Convert
        Building building = buildingConverter.toEntity(dto);

        // 2. XỬ LÝ TỰ ĐỘNG ĐIỀN THÔNG TIN QUẢN LÝ
        try {
            // Lấy tên đăng nhập hiện tại từ Token
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

            // Gọi Repository (Sử dụng findBy... và .orElse(null))
            User currentUser = userRepository.findByUserNameAndStatus(currentUsername, 1)
                    .orElse(null); // Nếu không tìm thấy thì trả về null

            if (currentUser != null) {
                // Nếu User không điền tên quản lý -> Lấy "fullName" của người đang đăng nhập
                if (building.getManagerName() == null || building.getManagerName().isEmpty()) {
                    building.setManagerName(currentUser.getFullName());
                }

                // Nếu User không điền SĐT -> Lấy SĐT của người đang đăng nhập (Nếu trong User
                // entity có phone)
                /*
                 * if (building.getManagerPhone() == null ||
                 * building.getManagerPhone().isEmpty()) {
                 * building.setManagerPhone(currentUser.getPhone());
                 * }
                 */
            }
        } catch (Exception e) {
            System.out.println("Không lấy được thông tin người dùng hiện tại: " + e.getMessage());
            // Không sao cả, cứ để trống nếu lỗi
        }

        // 3. Set trạng thái và Lưu
        building.setStatus(1);

        // Xử lý RentArea (như mình dặn ở bước trước)
        // ...

        Building savedBuilding = buildingRepository.save(building);
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

    @Override
    public void assignBuildingToStaffs(Long buildingId, List<Long> staffIds) {
        // 1. Kiểm tra tòa nhà có tồn tại không
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Tòa nhà không tồn tại!"));

        // 2. Xóa hết phân công cũ của tòa nhà này (Reset)
        assignmentBuildingRepository.deleteByBuilding_Id(buildingId);

        // 3. Nếu danh sách staffIds gửi lên không rỗng -> Tạo phân công mới
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

            // Lưu tất cả một lần (Batch insert)
            assignmentBuildingRepository.saveAll(assignments);
        }
    }
}