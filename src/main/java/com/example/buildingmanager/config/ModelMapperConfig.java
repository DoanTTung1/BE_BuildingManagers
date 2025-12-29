package com.example.buildingmanager.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        // Tạo object ModelMapper
        ModelMapper modelMapper = new ModelMapper();
        
        // Cấu hình: Chỉ map khi tên field và kiểu dữ liệu khớp chuẩn
        modelMapper.getConfiguration()
                   .setMatchingStrategy(MatchingStrategies.STANDARD);
        
        return modelMapper;
    }
}