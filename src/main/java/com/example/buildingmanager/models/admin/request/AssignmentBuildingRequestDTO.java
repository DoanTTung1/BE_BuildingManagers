package com.example.buildingmanager.models.admin.request;

import java.io.Serializable;
import java.util.List;

public class AssignmentBuildingRequestDTO implements Serializable {
    private Long BuildingId;
    private List<Long> StaffIds;

}
