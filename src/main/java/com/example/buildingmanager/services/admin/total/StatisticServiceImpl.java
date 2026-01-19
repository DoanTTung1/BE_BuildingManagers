package com.example.buildingmanager.services.admin.total;

import org.springframework.stereotype.Service;

import com.example.buildingmanager.models.admin.response.DashboardResponse;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements IStatisticService {
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardResponse getDashboardStatistics() {
        // 1. Logic nghiệp vụ nằm ở đây
        long totalBuildings = buildingRepository.count();
        long totalUsers = userRepository.count();

        // Giả lập hoặc gọi repository khác
        long totalMaintenance = 5;
        Double totalRevenue = 25000000.0;

        // 2. Build và trả về DTO
        return DashboardResponse.builder()
                .countBuildings(totalBuildings)
                .countCustomers(totalUsers)
                .countMaintenance(totalMaintenance)
                .totalRevenue(totalRevenue)
                .build();
    }
}