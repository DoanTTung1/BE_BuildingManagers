package com.example.buildingmanager.controllers.users;

import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.users.BuildingSearchDTO;
import com.example.buildingmanager.services.user.UserBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/building")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserBuildingController {

    @Autowired
    private UserBuildingService userBuildingService;

    @GetMapping
    // Sửa kiểu trả về thành List<BuildingSearchResponse>
    public List<BuildingSearchResponse> searchBuildings(@ModelAttribute BuildingSearchDTO model) {
        return userBuildingService.findAll(model);
    }
}