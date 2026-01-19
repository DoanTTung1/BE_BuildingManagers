package com.example.buildingmanager.controllers.admin;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.buildingmanager.models.admin.response.DashboardResponse;
import com.example.buildingmanager.services.admin.total.IStatisticService;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin("*")
public class StatisticController {

    @Autowired
    private IStatisticService statisticService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardStats() {
        // Controller giờ chỉ làm nhiệm vụ điều phối, code rất gọn
        DashboardResponse result = statisticService.getDashboardStatistics();
        return ResponseEntity.ok(result);
    }
}