package com.example.buildingmanager.config;

import com.example.buildingmanager.entities.Role;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.repositories.RoleRepository;
import com.example.buildingmanager.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Tạo các ROLE mặc định nếu chưa có (Tránh lỗi bảng Role trống)
        createRoleIfNotFound("ADMIN", "Quản trị hệ thống");
        createRoleIfNotFound("STAFF", "Nhân viên quản lý");
        createRoleIfNotFound("USER", "Khách hàng");

        // 2. Tạo tài khoản ADMIN mặc định nếu chưa có
        if (!userRepository.existsByUserName("admin")) {
            User admin = new User();
            admin.setUserName("admin");
            admin.setFullName("Super Admin");
            admin.setEmail("admin@building.com");
            admin.setPhone("0999999999");
            admin.setStatus(1); // 1 = Hoạt động
            
            // Mật khẩu là: 123456 (Đã mã hóa)
            admin.setPassword(passwordEncoder.encode("123456"));

            // Gán quyền ADMIN
            Role adminRole = roleRepository.findByCode("ADMIN");
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println(">>> ĐÃ TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH: admin / 123456");
        }
    }

    // Hàm phụ trợ để tạo Role
    private void createRoleIfNotFound(String code, String name) {
        if (roleRepository.findByCode(code) == null) {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            roleRepository.save(role);
            System.out.println(">>> Đã tạo Role: " + code);
        }
    }
}