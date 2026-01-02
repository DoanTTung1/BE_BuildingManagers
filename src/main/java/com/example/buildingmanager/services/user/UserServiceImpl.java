package com.example.buildingmanager.services.user;

import com.example.buildingmanager.entities.Role;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.mapper.UserConverter;
import com.example.buildingmanager.models.user.UserDTO;
import com.example.buildingmanager.models.user.UserUpdateRequest;
import com.example.buildingmanager.repositories.RoleRepository;
import com.example.buildingmanager.repositories.UserRepository;
import com.example.buildingmanager.services.upload.IStorageService;

import lombok.RequiredArgsConstructor;
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
    private final UserConverter userConverter; // Sử dụng cái này thay vì viết hàm convert tay

    // Service Upload ảnh
    private final IStorageService storageService;

    @Override
    public List<UserDTO> getAllUsers() {
        // Sử dụng userConverter để đảm bảo Avatar và OTP được map đúng
        return userRepository.findAll().stream()
                .map(userConverter::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAllStaffs() {
        List<User> staffs = userRepository.findByRoles_CodeAndStatus("STAFF", 1);
        return staffs.stream()
                .map(userConverter::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO createStaff(UserDTO dto) {
        // 1. Check trùng tên đăng nhập
        if (userRepository.existsByUserName(dto.getUsername())) { // DTO dùng username (chữ thường)
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        // 2. Map dữ liệu cơ bản (Dùng ModelMapper hoặc Converter đều được)
        // Lưu ý: Nếu dùng Converter.toEntity thì phải set lại password bên dưới
        User user = new User();
        user.setUserName(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());

        // 3. XỬ LÝ MẬT KHẨU
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("123456"));
        }

        // 4. Set Status Active
        user.setStatus(1);

        // 5. XỬ LÝ ROLE
        Set<Role> roles = new HashSet<>();
        // SỬA: DTO dùng 'roles' chứ không phải 'roleCodes'
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            for (String code : dto.getRoles()) {
                Role role = roleRepository.findByCode(code);
                if (role != null) {
                    roles.add(role);
                } else {
                    throw new RuntimeException("Không tìm thấy quyền: " + code);
                }
            }
        } else {
            // Mặc định là STAFF nếu không gửi quyền
            Role defaultRole = roleRepository.findByCode("STAFF");
            if (defaultRole != null)
                roles.add(defaultRole);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return userConverter.toUserDTO(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setStatus(0);
        userRepository.save(user);
    }

    @Override
    public UserDTO updateProfile(String currentUsername, UserUpdateRequest request) {
        // 1. Tìm user hiện tại
        User user = userRepository.findByUserNameAndStatus(currentUsername, 1)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // 2. Cập nhật thông tin cơ bản
        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }

        // Cập nhật Username (Check trùng)
        if (request.getUsername() != null && !request.getUsername().isEmpty()
                && !request.getUsername().equals(user.getUserName())) {
            if (userRepository.existsByUserName(request.getUsername())) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại!");
            }
            user.setUserName(request.getUsername());
        }

        // Cập nhật SĐT
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
            // user.setPhoneVerified(false); // Nếu muốn logic chặt chẽ: đổi SĐT xong phải
            // xác thực lại
        }

        // 3. XỬ LÝ UPLOAD ẢNH
        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            try {
                // Xóa ảnh cũ trên Cloudinary nếu có (để tiết kiệm dung lượng)
                if (user.getAvatar() != null && user.getAvatar().startsWith("http")) {
                    storageService.deleteFile(user.getAvatar());
                }

                // Upload ảnh mới
                String avatarUrl = storageService.storeFile(request.getAvatarFile());
                user.setAvatar(avatarUrl);

            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi upload ảnh: " + e.getMessage());
            }
        }

        // 4. Lưu và trả về
        User savedUser = userRepository.save(user);
        return userConverter.toUserDTO(savedUser);
    }
}