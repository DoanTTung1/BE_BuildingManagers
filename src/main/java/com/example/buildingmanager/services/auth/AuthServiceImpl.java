package com.example.buildingmanager.services.auth;

import com.example.buildingmanager.config.JwtTokenProvider;
import com.example.buildingmanager.entities.Role;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.models.auth.AuthResponse;
import com.example.buildingmanager.models.auth.ChangePasswordRequest;
import com.example.buildingmanager.models.auth.LoginRequest;
import com.example.buildingmanager.models.auth.RegisterRequest;
import com.example.buildingmanager.repositories.RoleRepository;
import com.example.buildingmanager.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.mail.SimpleMailMessage; // Import Mail
import org.springframework.mail.javamail.JavaMailSender; // Import Sender
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtTokenProvider tokenProvider;

        // --- BỔ SUNG: Mail Sender để gửi mail quên mật khẩu ---
        private final JavaMailSender mailSender;

        // --- BỔ SUNG: Lấy Google Client ID từ file cấu hình ---
        @Value("${google.client.id}")
        private String googleClientId;

        @Override
        @Transactional
        public AuthResponse login(LoginRequest request) {
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
        @Transactional
        public AuthResponse register(RegisterRequest request) {
                if (userRepository.existsByUserName(request.getUsername())) {
                        throw new RuntimeException("Lỗi: Username đã tồn tại!");
                }

                User user = new User();
                user.setUserName(request.getUsername());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setFullName(request.getFullName());
                user.setEmail(request.getEmail());
                user.setPhone(request.getPhone());
                user.setStatus(1);
                user.setPhoneVerified(false);

                // Gán Role
                Role userRole = roleRepository.findByCode("USER");
                if (userRole == null)
                        throw new RuntimeException("Lỗi: Không tìm thấy Role USER trong DB");
                user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

                // OTP
                String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
                user.setOtpCode(otp);
                user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

                User savedUser = userRepository.save(user);

                System.err.println("************************************************");
                System.err.println("MÃ OTP ĐĂNG KÝ CỦA [" + request.getUsername() + "] LÀ: " + otp);
                System.err.println("************************************************");

                try {
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(request.getUsername(),
                                                        request.getPassword()));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        String jwt = tokenProvider.generateToken(authentication);

                        return new AuthResponse(
                                        jwt, savedUser.getId(), savedUser.getUserName(),
                                        savedUser.getFullName(), savedUser.getEmail(),
                                        savedUser.getPhone(), null,
                                        Collections.singletonList("USER"), false);
                } catch (Exception e) {
                        return new AuthResponse(null, savedUser.getId(), savedUser.getUserName(),
                                        savedUser.getFullName(), savedUser.getEmail(),
                                        savedUser.getPhone(), null,
                                        Collections.singletonList("USER"), false);
                }
        }

        @Override
        @Transactional
        public void sendOtp(String userName) {
                User user = userRepository.findByUserNameAndStatus(userName, 1)
                                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

                String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
                user.setOtpCode(otp);
                user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
                userRepository.save(user);

                System.err.println("************************************************");
                System.err.println(">>> MÃ OTP CỦA [" + userName + "] LÀ: " + otp);
                System.err.println("************************************************");
        }

        @Override
        @Transactional
        public boolean verifyOtp(String userName, String otpInput) {
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

        // --- BỔ SUNG: IMPLEMENT HÀM QUÊN MẬT KHẨU ---
        @Override
        public void forgotPassword(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

                String newPassword = UUID.randomUUID().toString().substring(0, 8);
                user.setPassword(passwordEncoder.encode(newPassword)); // Mã hóa mật khẩu
                userRepository.save(user); // Lưu vào database

                SimpleMailMessage message = new SimpleMailMessage();

                // --- THÊM DÒNG NÀY (QUAN TRỌNG NHẤT) ---
                // Phải điền chính xác email bạn đã Verify bên SendGrid
                message.setFrom("photonics.pixel02558@gmail.com");

                message.setTo(email);
                message.setSubject("Cấp lại mật khẩu - EliteHomes");
                message.setText("Chào " + user.getFullName() + ",\n\n"
                                + "Mật khẩu mới của bạn là: " + newPassword + "\n\n"
                                + "Vui lòng đăng nhập và đổi lại mật khẩu ngay.");

                mailSender.send(message);
        }

        // --- CODE CHUẨN ĐỂ ĐĂNG NHẬP GOOGLE ---
        @Override
        @Transactional
        public AuthResponse loginWithGoogle(String credential) throws Exception {
                // 1. Xác thực Token với Google
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                                new GsonFactory())
                                .setAudience(Collections.singletonList(googleClientId))
                                .build();

                GoogleIdToken idToken = verifier.verify(credential);
                if (idToken == null)
                        throw new RuntimeException("Token Google không hợp lệ!");

                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                // 2. Tìm User hoặc Tạo mới
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                        user = new User();
                        user.setEmail(email);
                        user.setUserName(email);
                        user.setPassword(passwordEncoder.encode("GoogleLogin@123")); // Mật khẩu giả
                        user.setStatus(1);

                        Role userRole = roleRepository.findByCode("USER");
                        if (userRole != null) {
                                user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
                        }
                }

                // 3. Luôn cập nhật thông tin mới nhất (Avatar, Tên) từ Google
                user.setFullName(name);
                user.setAvatar(pictureUrl);
                userRepository.save(user); // Lưu User (Cả mới và cũ đều được lưu)

                // 4. Tạo JWT Token (Tạo Authentication giả để khớp với hàm login thường)
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                user.getUserName(), null, null);

                // Dùng generateToken(authentication) cho đồng bộ với hàm login
                String jwt = tokenProvider.generateToken(authentication);

                // 5. Trả về kết quả
                List<String> roleNames = user.getRoles().stream()
                                .map(Role::getCode)
                                .collect(Collectors.toList());

                return new AuthResponse(
                                jwt, user.getId(), user.getUserName(),
                                user.getFullName(), user.getEmail(), user.getPhone(),
                                user.getAvatar(), roleNames, false);
        }

        @Override
        @Transactional
        public void changePassword(String username, ChangePasswordRequest request) {
                // 1. Lấy thông tin user từ DB
                User user = userRepository.findByUserNameAndStatus(username, 1)
                                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

                // 2. Kiểm tra mật khẩu cũ có đúng không
                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
                }

                // 3. Kiểm tra mật khẩu mới và xác nhận có khớp nhau không
                if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                        throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp!");
                }

                // 4. Đổi mật khẩu và lưu xuống DB
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);
        }
}