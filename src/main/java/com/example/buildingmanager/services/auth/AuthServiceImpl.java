package com.example.buildingmanager.services.auth;

import com.example.buildingmanager.config.JwtTokenProvider;
import com.example.buildingmanager.entities.Role;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.models.auth.AuthResponse;
import com.example.buildingmanager.models.auth.LoginRequest;
import com.example.buildingmanager.models.auth.RegisterRequest;
import com.example.buildingmanager.repositories.RoleRepository;
import com.example.buildingmanager.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // Cần import để xử lý hạn OTP
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
                // Giữ nguyên logic cũ nhưng đảm bảo dùng request.getUsername()
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = tokenProvider.generateToken(authentication);

                User userDetails = userRepository.findByUserNameAndStatus(request.getUsername(), 1)
                                .orElseThrow(() -> new RuntimeException("User không tồn tại hoặc đã bị khóa!"));

                List<String> roles = userDetails.getRoles().stream()
                                .map(Role::getCode)
                                .collect(Collectors.toList());

                return new AuthResponse(jwt, userDetails.getId(), userDetails.getUserName(),
                                userDetails.getFullName(), userDetails.getEmail(), userDetails.getPhone(),
                                userDetails.getAvatar(), roles, userDetails.isPhoneVerified());
        }

        @Override
        @Transactional // Gắn lại Transactional để đảm bảo tính toàn vẹn dữ liệu
        public AuthResponse register(RegisterRequest request) {
                // 1. Kiểm tra trùng username
                if (userRepository.existsByUserName(request.getUsername())) {
                        throw new RuntimeException("Lỗi: Username đã tồn tại!");
                }

                // 2. Tạo đối tượng User và map dữ liệu từ Request
                User user = new User();
                user.setUserName(request.getUsername());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setFullName(request.getFullName());
                user.setEmail(request.getEmail());
                user.setPhone(request.getPhone());
                user.setStatus(1);
                user.setPhoneVerified(false);

                // 3. Gán Role mặc định
                Role userRole = roleRepository.findByCode("USER");
                user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

                // 4. Sinh mã OTP ngay lúc đăng ký để lưu vào DB luôn
                String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
                user.setOtpCode(otp);
                user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

                // 5. Lưu vào Database
                User savedUser = userRepository.save(user);

                // 6. In mã OTP ra Log Railway để bạn lấy mã xác thực
                System.err.println("************************************************");
                System.err.println("MÃ OTP ĐĂNG KÝ CỦA [" + request.getUsername() + "] LÀ: " + otp);
                System.err.println("************************************************");

                // 7. Sinh Token JWT để user đăng nhập luôn sau khi đăng ký thành công
                // Lưu ý: Phần authenticate nên để sau khi đã save thành công
                try {
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(request.getUsername(),
                                                        request.getPassword()));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        String jwt = tokenProvider.generateToken(authentication);

                        return new AuthResponse(
                                        jwt,
                                        savedUser.getId(),
                                        savedUser.getUserName(),
                                        savedUser.getFullName(),
                                        savedUser.getEmail(),
                                        savedUser.getPhone(),
                                        null,
                                        Collections.singletonList("USER"),
                                        false);
                } catch (Exception e) {
                        // Nếu lỗi xác thực tự động, vẫn trả về response nhưng ko có token
                        return new AuthResponse(null, savedUser.getId(), savedUser.getUserName(),
                                        savedUser.getFullName(), savedUser.getEmail(),
                                        savedUser.getPhone(), null,
                                        Collections.singletonList("USER"), false);
                }
        }

        // --- CÁC HÀM XỬ LÝ OTP ---

        @Override
        @Transactional
        public void sendOtp(String userName) { // Đổi thành userName cho đồng bộ
                User user = userRepository.findByUserNameAndStatus(userName, 1)
                                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

                String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
                user.setOtpCode(otp);
                user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
                userRepository.save(user);

                // Dùng System.err để mã hiện màu đỏ/nổi bật trong Logs Railway
                System.err.println("************************************************");
                System.err.println(">>> MÃ OTP CỦA [" + userName + "] LÀ: " + otp);
                System.err.println("************************************************");
        }

        @Override
        @Transactional
        public boolean verifyOtp(String userName, String otpInput) { // Đổi thành userName
                User user = userRepository.findByUserNameAndStatus(userName, 1)
                                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

                if (user.getOtpCode() == null || !user.getOtpCode().equals(otpInput)) {
                        throw new RuntimeException("Mã OTP không chính xác!");
                }

                if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
                        throw new RuntimeException("Mã OTP đã hết hạn!");
                }

                user.setPhoneVerified(true);
                user.setOtpCode(null);
                user.setOtpExpiry(null);
                userRepository.save(user);

                return true;
        }
}