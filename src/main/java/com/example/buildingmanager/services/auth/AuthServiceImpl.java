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
                // 1. Xác thực Username/Password
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUserName(),
                                                request.getPassword()));

                // 2. Lưu context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 3. Sinh Token
                String jwt = tokenProvider.generateToken(authentication);

                // 4. Lấy thông tin User từ DB
                User userDetails = userRepository.findByUserNameAndStatus(request.getUserName(), 1)
                                .orElseThrow(() -> new RuntimeException("User không tồn tại hoặc đã bị khóa!"));

                // 5. Lấy Roles
                List<String> roles = userDetails.getRoles().stream()
                                .map(Role::getCode)
                                .collect(Collectors.toList());

                // 6. Trả về AuthResponse (Có kèm trạng thái phoneVerified)
                return new AuthResponse(
                                jwt,
                                userDetails.getId(),
                                userDetails.getUserName(),
                                userDetails.getEmail(),
                                roles,
                                userDetails.isPhoneVerified() // Trả về true/false thực tế từ DB
                );
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
                user.setStatus(1); // Mặc định kích hoạt

                // Mặc định đăng ký xong là CHƯA xác thực
                user.setPhoneVerified(false);

                // 3. Gán Role mặc định là USER
                Role userRole = roleRepository.findByCode("USER");
                if (userRole == null) {
                        throw new RuntimeException("Lỗi: Chưa có Role USER trong hệ thống!");
                }
                user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

                // 4. Lưu vào DB
                User savedUser = userRepository.save(user);

                // 5. Tự động đăng nhập (Auto Login)
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUserName(),
                                                request.getPassword()));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Sinh token
                String jwt = tokenProvider.generateToken(authentication);

                // 6. Trả về
                return new AuthResponse(
                                jwt,
                                savedUser.getId(),
                                savedUser.getUserName(),
                                savedUser.getEmail(),
                                Collections.singletonList("USER"),
                                false // Mới đăng ký thì chắc chắn chưa xác thực
                );
        }

        // --- CÁC HÀM XỬ LÝ OTP (BỔ SUNG) ---

        @Override
        public void sendOtp(String username) {
                User user = userRepository.findByUserNameAndStatus(username, 1)
                                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

                // 1. Tạo mã OTP ngẫu nhiên 6 chữ số
                String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));

                // 2. Lưu OTP và thời gian hết hạn (5 phút) vào DB
                user.setOtpCode(otp);
                user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
                userRepository.save(user);

                // 3. Giả lập gửi tin nhắn (In ra console để test)
                // Trong thực tế, bạn sẽ gọi API của Twilio/eSMS/Viettel tại đây
                System.out.println("============================================");
                System.out.println(">>> OTP GỬI ĐẾN SỐ " + user.getPhone() + ": " + otp);
                System.out.println("============================================");
        }

        @Override
        public boolean verifyOtp(String username, String otpInput) {
                User user = userRepository.findByUserNameAndStatus(username, 1)
                                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

                // 1. Kiểm tra mã OTP có khớp không
                if (user.getOtpCode() == null || !user.getOtpCode().equals(otpInput)) {
                        throw new RuntimeException("Mã OTP không chính xác!");
                }

                // 2. Kiểm tra thời hạn
                if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
                        throw new RuntimeException("Mã OTP đã hết hạn! Vui lòng lấy mã mới.");
                }

                // 3. Nếu đúng hết -> Update trạng thái đã xác thực
                user.setPhoneVerified(true);
                user.setOtpCode(null); // Xóa OTP cũ để không dùng lại được
                user.setOtpExpiry(null);
                userRepository.save(user);

                return true;
        }
}