package com.example.buildingmanager.services.admin.building;

import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;

import java.util.List;

public interface BuildingService {
    List<BuildingSearchResponse> findAll(BuildingSearchBuilder builder);
    UpdateAndCreateBuildingDTO updateBuilding(UpdateAndCreateBuildingDTO buildingDTO);
    UpdateAndCreateBuildingDTO createBuilding(UpdateAndCreateBuildingDTO dto);
    void softDeleteBuilding(Long id);
    void hardDeleteBuilding(Long id);
    BuildingSearchResponse findById(Long id);
}
