package com.example.buildingmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Cấu hình CORS: Trỏ thẳng vào cái Bean bên dưới
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Tắt CSRF (để API gọi được)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 3. Cấp quyền (Hiện tại bạn đang cho phép tất cả - OK để test)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }

    // 👇 ĐÂY LÀ CÁI BẠN ĐANG THIẾU 👇
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // DANH SÁCH KHÁCH MỜI (QUAN TRỌNG NHẤT)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",                    // Cho phép máy local
            "https://fe-building-managers.vercel.app"   // 👈 LINK VERCEL CỦA BẠN (Không có dấu / ở cuối)
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}