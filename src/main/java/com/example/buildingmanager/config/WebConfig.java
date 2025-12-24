package com.example.buildingmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 1. Áp dụng cho tất cả các API trong hệ thống
                .allowedOrigins("http://localhost:3000") // 2. Chỉ cho phép Frontend React (port 3000) gọi vào
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 3. Các method được phép
                .allowedHeaders("*") // 4. Cho phép tất cả các Header (như Authorization...)
                .allowCredentials(true); // 5. Cho phép gửi cookie/token nếu cần
    }
}