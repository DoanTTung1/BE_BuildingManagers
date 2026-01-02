package com.example.buildingmanager.config;

import com.example.buildingmanager.security.JwtAuthenticationFilter;
import com.example.buildingmanager.services.auth.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Cấu hình CORS (Để Frontend React gọi được API)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Tắt CSRF (Do dùng Token JWT nên không cần cái này)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Stateless Session (Server không lưu trạng thái đăng nhập, dùng Token để
                // nhớ)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. PHÂN QUYỀN (QUAN TRỌNG NHẤT)
                .authorizeHttpRequests(auth -> auth
                        // === NHÓM 1: CÔNG KHAI (PUBLIC - Ai cũng vào được) ===
                        .requestMatchers("/api/auth/**").permitAll() // Đăng ký, Đăng nhập
                        .requestMatchers("/api/customers/contact").permitAll() // Gửi liên hệ
                        .requestMatchers("/images/**", "/css/**", "/js/**").permitAll() // File tĩnh
                        .requestMatchers("/error").permitAll() // Cho phép trang lỗi mặc định
                        .requestMatchers("/api/users/profile/update").authenticated()

                        // Chỉ cho phép GET danh sách và chi tiết (Khách xem nhà)
                        .requestMatchers(HttpMethod.GET, "/api/buildings", "/api/buildings/{id}").permitAll()

                        // === NHÓM 2: UPLOAD FILE ===
                        // Yêu cầu: Phải đăng nhập mới được Upload (Token hợp lệ)
                        .requestMatchers("/api/upload/**").authenticated()

                        // === NHÓM 3: API RIÊNG CHO ADMIN/STAFF ===
                        .requestMatchers("/api/buildings/admin").hasAnyRole("ADMIN", "STAFF")

                        // === NHÓM 4: QUẢN LÝ TÒA NHÀ (Cần quyền cụ thể) ===
                        // Thêm mới: USER, STAFF, ADMIN đều được (Miễn là đã đăng nhập và có role)
                        .requestMatchers(HttpMethod.POST, "/api/buildings").hasAnyRole("ADMIN", "STAFF", "USER")

                        // Cập nhật: Chỉ nhân viên hoặc Admin
                        .requestMatchers(HttpMethod.PUT, "/api/buildings/**").hasAnyRole("ADMIN", "STAFF")

                        // Xóa & Giao việc: Chỉ trùm cuối ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/buildings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/buildings/*/assignment").hasRole("ADMIN")

                        // === NHÓM 5: QUẢN LÝ NGƯỜI DÙNG ===
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // === OPTIONS REQUEST (QUAN TRỌNG CHO CORS) ===
                        // Cho phép tất cả các request OPTIONS (Preflight) đi qua mà không cần check
                        // quyền
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // === NHÓM 6: CÁC API KHÁC ===
                        .anyRequest().authenticated())

                // 5. Cấu hình Provider xác thực
                .authenticationProvider(authenticationProvider())

                // 6. Thêm Filter kiểm tra Token trước khi vào các bước trên
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- CÁC BEAN CẤU HÌNH BỔ TRỢ ---

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService); // Load user từ DB
        authProvider.setPasswordEncoder(passwordEncoder()); // So sánh pass mã hóa
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- CẤU HÌNH CORS (SỬA LẠI ĐỂ FIX LỖI 403) ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Cho phép các Domain này (Vercel & Localhost)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://thanhtung-building.vercel.app" // Frontend trên Vercel
        ));

        // 2. Cho phép TẤT CẢ các method
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));

        // 3. [QUAN TRỌNG] Cho phép TẤT CẢ các Header
        // Thay vì liệt kê từng cái, ta dùng "*" để tránh bị thiếu header gây lỗi 403
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 4. Cho phép gửi Credentials (Cookie, Auth Header)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}