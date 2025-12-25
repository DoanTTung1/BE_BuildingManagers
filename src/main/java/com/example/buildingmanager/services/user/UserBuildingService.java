package com.example.buildingmanager.services.user;

import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.users.BuildingSearchDTO;
import java.util.List;

public interface UserBuildingService {
    List<BuildingSearchResponse> findAll(BuildingSearchDTO searchDTO);
}