package com.example.buildingmanager.services.auth;

import com.example.buildingmanager.entities.Role;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.models.auth.AuthResponse;
import com.example.buildingmanager.models.auth.LoginRequest;
import com.example.buildingmanager.models.auth.RegisterRequest;
import com.example.buildingmanager.repositories.RoleRepository;
import com.example.buildingmanager.repositories.UserRepository;
import com.example.buildingmanager.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Xác thực (Sẽ gọi CustomUserDetailsService để kiểm tra DB)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserName(),
                        request.getPassword()));

        // 2. Set Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Sinh Token
        // SỬA: Truyền 'authentication' vào thay vì 'request.getUserName()'
        // Để bên trong lấy được cả Username và Roles
        String jwt = tokenProvider.generateToken(authentication);

        // 4. Lấy thông tin User từ DB (Để trả về ID, Email... cho Frontend)
        User userDetails = userRepository.findByUserNameAndStatus(request.getUserName(), 1)
                .orElseThrow(() -> new RuntimeException("User không tồn tại hoặc đã bị khóa!"));

        // 5. Lấy Roles
        List<String> roles = userDetails.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        // 6. Trả về
        return new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUserName(),
                userDetails.getEmail(),
                roles);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Kiểm tra trùng
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new RuntimeException("Lỗi: Username đã tồn tại!");
        }

        // 2. Tạo User mới
        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);

        // 3. Gán Role mặc định là USER
        Role userRole = roleRepository.findByCode("USER");
        if (userRole == null)
            throw new RuntimeException("Lỗi: Chưa có Role USER trong hệ thống!");

        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        // 4. Lưu vào DB
        User savedUser = userRepository.save(user);

        // 5. TỰ ĐỘNG ĐĂNG NHẬP
        // SỬA: Phải xác thực lại để lấy được object Authentication đầy đủ quyền (có
        // ROLE_)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserName(),
                        request.getPassword() // Dùng pass thô chưa mã hóa từ request
                ));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sinh token từ object authentication chuẩn
        String jwt = tokenProvider.generateToken(authentication);

        // 6. Trả về AuthResponse
        return new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getUserName(),
                savedUser.getEmail(),
                Collections.singletonList("USER"));
    }
}