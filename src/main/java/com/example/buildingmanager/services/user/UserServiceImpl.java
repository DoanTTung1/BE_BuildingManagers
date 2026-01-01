package com.example.buildingmanager.services.user;

import com.example.buildingmanager.entities.Role;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.models.user.UserDTO;
import com.example.buildingmanager.repositories.RoleRepository;
import com.example.buildingmanager.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    private UserDTO convertToDTO(User user) {
        UserDTO dto = modelMapper.map(user, UserDTO.class);
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
        dto.setRoleCodes(roles);
        return dto;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAllStaffs() {
        List<User> staffs = userRepository.findByRoles_CodeAndStatus("STAFF", 1);
        return staffs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // === ĐÂY LÀ HÀM CẦN SỬA ===
    @Override
    public UserDTO createStaff(UserDTO dto) {
        // 1. Check trùng tên đăng nhập
        if (userRepository.existsByUserName(dto.getUserName())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        // 2. Map dữ liệu cơ bản
        User user = modelMapper.map(dto, User.class);

        // 3. XỬ LÝ MẬT KHẨU (SỬA: Lấy từ DTO thay vì cứng 123456)
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            // Fallback nếu frontend quên gửi (mặc định)
            user.setPassword(passwordEncoder.encode("123456"));
        }

        // 4. Set Status Active
        user.setStatus(1);

        // 5. XỬ LÝ ROLE (SỬA: Lấy từ danh sách roleCodes frontend gửi lên)
        Set<Role> roles = new HashSet<>();

        if (dto.getRoleCodes() != null && !dto.getRoleCodes().isEmpty()) {
            // Duyệt qua từng code gửi lên (ví dụ: ["ADMIN"] hoặc ["STAFF"])
            for (String code : dto.getRoleCodes()) {
                Role role = roleRepository.findByCode(code);
                if (role != null) {
                    roles.add(role);
                } else {
                    // Tùy chọn: Báo lỗi nếu gửi role bậy bạ
                    throw new RuntimeException("Không tìm thấy quyền: " + code);
                }
            }
        } else {
            // Nếu không gửi gì, mặc định là STAFF
            Role defaultRole = roleRepository.findByCode("STAFF");
            if (defaultRole != null)
                roles.add(defaultRole);
        }

        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setStatus(0);
        userRepository.save(user);
    }
}