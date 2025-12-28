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
                // 1. Cấu hình CORS (Cho phép Frontend gọi API)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Tắt CSRF (Do dùng Token nên không cần)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Stateless Session (Không lưu session trên server)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Phân quyền chi tiết (Authorize Requests)
                .authorizeHttpRequests(auth -> auth
                        // --- NHÓM PUBLIC (Ai cũng được vào) ---
                        .requestMatchers("/api/auth/**").permitAll() // Đăng ký/Đăng nhập
                        .requestMatchers("/api/customers/contact").permitAll() // Khách gửi form liên hệ
                        .requestMatchers(HttpMethod.GET, "/api/buildings/**").permitAll() // Xem danh sách nhà
                        .requestMatchers("/images/**", "/css/**", "/js/**").permitAll() // Tài nguyên tĩnh

                        // --- NHÓM NGƯỜI DÙNG (USER) ---
                        // Được phép đăng tin (POST)
                        .requestMatchers(HttpMethod.POST, "/api/buildings").hasAnyRole("ADMIN", "STAFF", "USER")

                        // --- NHÓM QUẢN LÝ (ADMIN & STAFF) ---
                        // Được sửa tin (PUT)
                        .requestMatchers(HttpMethod.PUT, "/api/buildings/**").hasAnyRole("ADMIN", "STAFF")
                        // Xem danh sách khách hàng
                        .requestMatchers("/api/customers/**").hasAnyRole("ADMIN", "STAFF")

                        // --- NHÓM TỐI CAO (ADMIN) ---
                        // Chỉ Admin mới được XÓA (DELETE) tòa nhà hoặc quản lý User
                        .requestMatchers(HttpMethod.DELETE, "/api/buildings/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // --- CÁC REQUEST KHÁC ---
                        .anyRequest().authenticated())

                // 5. Thêm Filter kiểm tra Token trước
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- CÁC BEAN CẤU HÌNH PHỤ TRỢ ---

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
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

    // Cấu hình CORS để Frontend (React/Vue/Angular) không bị chặn
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép các nguồn này gọi API (Sửa lại port frontend của bạn nếu khác)
        configuration
                .setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173", "http://localhost:4200","https://thanhtung-building.vercel.app"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}