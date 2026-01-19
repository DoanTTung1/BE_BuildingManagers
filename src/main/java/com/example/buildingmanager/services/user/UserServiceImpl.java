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
    private final UserConverter userConverter;
    private final IStorageService storageService;

    // 👇 1. SỬA: Hàm này thay thế getAllUsers cũ để hỗ trợ lọc
    @Override
    public List<UserDTO> getUsersByStatus(Integer status) {
        List<User> users;
        if (status != null) {
            // Nếu FE gửi status (VD: 0 -> Thùng rác)
            users = userRepository.findByStatus(status);
        } else {
            // Nếu không gửi -> Lấy tất cả trừ thùng rác (Active)
            users = userRepository.findByStatusNot(0);
        }
        return users.stream().map(userConverter::toUserDTO).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAllStaffs() {
        return userRepository.findByRoles_CodeAndStatus("STAFF", 1).stream()
                .map(userConverter::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO createStaff(UserDTO dto) {
        if (userRepository.existsByUserName(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }

        User user = new User();
        user.setUserName(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(1); // Active

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("123456"));
        }

        Set<Role> roles = new HashSet<>();
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            for (String code : dto.getRoles()) {
                Role role = roleRepository.findByCode(code);
                if (role != null)
                    roles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByCode("STAFF");
            if (defaultRole != null)
                roles.add(defaultRole);
        }
        user.setRoles(roles);

        return userConverter.toUserDTO(userRepository.save(user));
    }

    // 👇 2. Xóa mềm (Soft Delete)
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setStatus(0);
        userRepository.save(user);
    }

    // 👇 3. BỔ SUNG: Xóa cứng (Hard Delete)
    @Override
    public void hardDeleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User không tồn tại");
        }
        // Lưu ý: Nếu user này có liên quan đến các bảng khác (VD: đang quản lý tòa
        // nhà),
        // bạn cần xóa liên kết đó trước khi deleteById để tránh lỗi Foreign Key.
        userRepository.deleteById(id);
    }

    // 👇 4. BỔ SUNG: Khôi phục (Restore)
    @Override
    public void restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setStatus(1); // Active lại
        userRepository.save(user);
    }

    @Override
    public UserDTO updateProfile(String currentUsername, UserUpdateRequest request) {
        User user = userRepository.findByUserNameAndStatus(currentUsername, 1)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email này đã được sử dụng!");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getUsername() != null && !request.getUsername().isEmpty()
                && !request.getUsername().equals(user.getUserName())) {
            if (userRepository.existsByUserName(request.getUsername())) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại!");
            }
            user.setUserName(request.getUsername());
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
        }

        // Logic đổi mật khẩu
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        // Upload ảnh
        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            try {
                if (user.getAvatar() != null && user.getAvatar().startsWith("http")) {
                    storageService.deleteFile(user.getAvatar());
                }
                String avatarUrl = storageService.storeFile(request.getAvatarFile());
                user.setAvatar(avatarUrl);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
            }
        }

        return userConverter.toUserDTO(userRepository.save(user));
    }
}