package com.example.buildingmanager.services.admin.total;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.buildingmanager.mapper.BuildingConverter; // Import đúng package mapper của bạn
import com.example.buildingmanager.entities.Building; // Import đúng entity
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.admin.response.DashboardResponse;
import com.example.buildingmanager.models.admin.response.MonthlyRevenueResponse;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements IStatisticService {

    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final BuildingConverter buildingConverter;

    @Override
    public DashboardResponse getDashboardStatistics() {
        // 1. Số liệu tổng
        long countBuildings = buildingRepository.count();
        long countUsers = userRepository.count();
        long countMaintenance = 0; // Tạm thời để 0

        // Tính tổng tiền (Xử lý null nếu DB trống)
        Double totalRevenue = buildingRepository.sumTotalRentPrice();
        if (totalRevenue == null) {
            totalRevenue = 0.0;
        }

        // 2. Lấy 5 tòa nhà mới nhất (Sửa tên hàm convert ở đây!)
        List<Building> recentBuildingEntities = buildingRepository.findTop5ByOrderByIdDesc();
        List<BuildingSearchResponse> recentBuildings = recentBuildingEntities.stream()
                .map(buildingConverter::toResponseDTO) // <--- ĐÃ SỬA: Dùng đúng hàm toResponseDTO của bạn
                .collect(Collectors.toList());

        // 3. Dữ liệu biểu đồ
        List<Object[]> rawChartData = buildingRepository.sumRentPriceGroupedByMonth();
        List<MonthlyRevenueResponse> chartData = new ArrayList<>();

        for (Object[] row : rawChartData) {
            // row[0]=Tháng, row[1]=Năm, row[2]=Tổng tiền
            String monthLabel = "Tháng " + row[0] + "/" + row[1];
            Double revenue = (Double) row[2];

            chartData.add(new MonthlyRevenueResponse(monthLabel, revenue));
        }

        return DashboardResponse.builder()
                .countBuildings(countBuildings)
                .countCustomers(countUsers)
                .countMaintenance(countMaintenance)
                .totalRevenue(totalRevenue)
                .monthlyRevenues(chartData)
                .recentBuildings(recentBuildings)
                .build();
    }
}