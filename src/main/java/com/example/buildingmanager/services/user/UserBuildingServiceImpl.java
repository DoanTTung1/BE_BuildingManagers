package com.example.buildingmanager.services.user;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.mapper.BuildingConverter;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.users.BuildingSearchDTO;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.specifications.BuildingSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service 
public class UserBuildingServiceImpl implements UserBuildingService {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private BuildingConverter buildingConverter; // Gọi cái Converter bạn vừa gửi

    public List<BuildingSearchResponse> findAll(BuildingSearchDTO searchDTO) {
        // 1. Lấy danh sách Entity từ DB
        Specification<Building> spec = BuildingSpecification.build(searchDTO);
        List<Building> buildings = buildingRepository.findAll(spec);
        
        // 2. Dùng Converter chuyển sang DTO (Dùng hàm toResponseDTO có sẵn)
        return buildings.stream()
                .map(item -> buildingConverter.toResponseDTO(item))
                .collect(Collectors.toList());
    }
}