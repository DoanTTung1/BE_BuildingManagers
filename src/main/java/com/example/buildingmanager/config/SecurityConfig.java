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
import java.util.List;

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

                        // Chỉ cho phép GET danh sách và chi tiết (Khách xem nhà)
                        .requestMatchers(HttpMethod.GET, "/api/buildings", "/api/buildings/{id}").permitAll()

                        // === NHÓM 2: UPLOAD FILE (MỚI THÊM) ===
                        // Yêu cầu: Phải đăng nhập mới được Upload (Token hợp lệ)
                        .requestMatchers("/api/upload/**").authenticated()

                        // === NHÓM 3: API RIÊNG CHO ADMIN/STAFF ===
                        // API này trả về cả tòa nhà đã xóa/ẩn -> Bắt buộc phải đăng nhập
                        .requestMatchers("/api/buildings/admin").hasAnyRole("ADMIN", "STAFF")

                        // === NHÓM 4: QUẢN LÝ TÒA NHÀ (Cần quyền cụ thể) ===
                        // Thêm mới: USER, STAFF, ADMIN đều được
                        .requestMatchers(HttpMethod.POST, "/api/buildings").hasAnyRole("ADMIN", "STAFF", "USER")

                        // Cập nhật: Chỉ nhân viên hoặc Admin
                        .requestMatchers(HttpMethod.PUT, "/api/buildings/**").hasAnyRole("ADMIN", "STAFF")

                        // Xóa & Giao việc: Chỉ trùm cuối ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/buildings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/buildings/*/assignment").hasRole("ADMIN")

                        // === NHÓM 5: QUẢN LÝ NGƯỜI DÙNG ===
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // === NHÓM 6: CÁC API KHÁC ===
                        // Mặc định các link chưa khai báo ở trên thì phải đăng nhập mới được vào
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

    // Cấu hình CORS chi tiết
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép các trang Web này gọi API (Frontend của bạn)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://thanhtung-building.vercel.app"));

        // Cho phép các method này
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Cho phép gửi kèm Header (như Authorization, Content-Type)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));

        // Cho phép gửi kèm credentials (nếu cần cookie)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}