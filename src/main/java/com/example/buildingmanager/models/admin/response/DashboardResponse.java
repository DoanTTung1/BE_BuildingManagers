package com.example.buildingmanager.models.admin.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
    private long countBuildings; // Số lượng tòa nhà
    private long countCustomers; // Số lượng khách hàng (User)
    private long countMaintenance; // Số yêu cầu bảo trì (Fix cứng hoặc đếm thật)
    private Double totalRevenue; // Doanh thu (nếu có)
    private List<MonthlyRevenueResponse> monthlyRevenues; // Dữ liệu biểu đồ cột
    private List<BuildingSearchResponse> recentBuildings;
}