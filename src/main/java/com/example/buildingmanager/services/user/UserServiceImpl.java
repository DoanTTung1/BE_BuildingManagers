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

    // Hàm chuyển đổi từ Entity -> DTO (Viết tay hoặc dùng Mapper + Custom)
    private UserDTO convertToDTO(User user) {
        UserDTO dto = modelMapper.map(user, UserDTO.class);

        // Tự xử lý phần Role (Lấy code từ Set<Role> bỏ vào List<String>)
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
        // Gọi hàm repository mới (findByRoles_Code...)
        List<User> staffs = userRepository.findByRoles_CodeAndStatus("STAFF", 1);
        return staffs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO createStaff(UserDTO dto) {
        if (userRepository.existsByUserName(dto.getUserName())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        User user = modelMapper.map(dto, User.class);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setStatus(1);

        // --- SỬA PHẦN GÁN ROLE ---
        Role staffRole = roleRepository.findByCode("STAFF");
        if (staffRole == null)
            throw new RuntimeException("Chưa có Role STAFF trong DB!");

        // Vì là ManyToMany nên phải tạo Set và add vào
        Set<Role> roles = new HashSet<>();
        roles.add(staffRole);
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