package com.example.buildingmanager.models.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyRevenueResponse {
    private String month; 
    private Double revenue; 
}
