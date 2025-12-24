package com.example.buildingmanager.mapper;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.District;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
@Component
public class BuildingConverter {

    /**
     * Chuyển từ DTO -> Entity (dùng cho create hoặc update mới)
     */
    public BuildingSearchResponse toResponseDTO(Building e) {
        String districName = e.getDistrict() != null ? e.getDistrict().getName() : "";
        String address = e.getStreet() + ", " + e.getWard() + ", " + districName;
        String rentAreaResult = "";
        if (e.getRentAreas() != null && !e.getRentAreas().isEmpty()) {
            rentAreaResult = e.getRentAreas().stream()
                    .map(item -> item.getValue().toString()) // Chuyển số sang chuỗi
                    .collect(Collectors.joining(", "));      // Nối bằng dấu phẩy
        }
        return BuildingSearchResponse.builder()
                .id(e.getId())
                .address(address)
                .emptyArea(rentAreaResult)
                .name(e.getName())
                .numberOfBasement(e.getNumberOfBasement())
                .managerName(e.getManagerName())
                .managerPhone(e.getManagerPhoneNumber())
                .floorArea(e.getFloorArea())
                .rentPrice(e.getRentPrice())
                .serviceFee(e.getServiceFee())
                .avatar(e.getAvatar())
                .brokerageFee((e.getBrokerageFee() != null ? e.getBrokerageFee().toString() : ""))
                .build();
    }

    public Building toEntity(UpdateAndCreateBuildingDTO dto) {
        District district = null;
        if (dto.getDistrictId() != null) {
            district = District.builder().id(dto.getDistrictId()).build();
        }

        return Building.builder()
                .name(dto.getName())
                .street(dto.getStreet())
                .ward(dto.getWard())
                .district(district)
                .structure(dto.getStructure())
                .numberOfBasement(dto.getNumberOfBasement())
                .floorArea(dto.getFloorArea())
                .direction(dto.getDirection())
                .level(dto.getLevel())
                .rentPrice(dto.getRentPrice())
                .rentPriceDescription(dto.getRentPriceDescription())
                .serviceFee(dto.getServiceFee())
                .carFee(dto.getCarFee())
                .motorbikeFee(dto.getMotorbikeFee())
                .overtimeFee(dto.getOvertimeFee())
                .waterFee(dto.getWaterFee())
                .electricityFee(dto.getElectricityFee())
                .deposit(dto.getDeposit())
                .payment(dto.getPayment())
                .rentTime(dto.getRentTime())
                .decorationTime(dto.getDecorationTime())
                .brokerageFee(dto.getBrokerageFee())
                .note(dto.getNote())
                .linkOfBuilding(dto.getLinkOfBuilding())
                .map(dto.getMap())
                .image(dto.getImage())
                .managerName(dto.getManagerName())
                .managerPhoneNumber(dto.getManagerPhoneNumber())
                .avatar(dto.getAvatar())
                .build();
    }

    /**
     * Chuyển từ Entity -> DTO (dùng để trả dữ liệu ra API)
     */
    public UpdateAndCreateBuildingDTO toDTO(Building entity) {
        return UpdateAndCreateBuildingDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .street(entity.getStreet())
                .ward(entity.getWard())
                .districtId(entity.getDistrict() != null ? entity.getDistrict().getId() : null)
                .structure(entity.getStructure())
                .numberOfBasement(entity.getNumberOfBasement())
                .floorArea(entity.getFloorArea())
                .direction(entity.getDirection())
                .level(entity.getLevel())
                .rentPrice(entity.getRentPrice())
                .rentPriceDescription(entity.getRentPriceDescription())
                .serviceFee(entity.getServiceFee())
                .carFee(entity.getCarFee())
                .motorbikeFee(entity.getMotorbikeFee())
                .overtimeFee(entity.getOvertimeFee())
                .waterFee(entity.getWaterFee())
                .electricityFee(entity.getElectricityFee())
                .deposit(entity.getDeposit())
                .payment(entity.getPayment())
                .rentTime(entity.getRentTime())
                .decorationTime(entity.getDecorationTime())
                .brokerageFee(entity.getBrokerageFee())
                .note(entity.getNote())
                .linkOfBuilding(entity.getLinkOfBuilding())
                .map(entity.getMap())
                .image(entity.getImage())
                .managerName(entity.getManagerName())
                .managerPhoneNumber(entity.getManagerPhoneNumber())
                .avatar(entity.getAvatar())
                .build();
    }

    /**
     * Hàm cập nhật entity cũ từ DTO (dùng cho update)
     */
    public void updateEntity(UpdateAndCreateBuildingDTO dto, Building entity) {
        if (dto.getDistrictId() != null) {
            entity.setDistrict(District.builder().id(dto.getDistrictId()).build());
        }
        entity.setName(dto.getName());
        entity.setStreet(dto.getStreet());
        entity.setWard(dto.getWard());
        entity.setStructure(dto.getStructure());
        entity.setNumberOfBasement(dto.getNumberOfBasement());
        entity.setFloorArea(dto.getFloorArea());
        entity.setDirection(dto.getDirection());
        entity.setLevel(dto.getLevel());
        entity.setRentPrice(dto.getRentPrice());
        entity.setRentPriceDescription(dto.getRentPriceDescription());
        entity.setServiceFee(dto.getServiceFee());
        entity.setCarFee(dto.getCarFee());
        entity.setMotorbikeFee(dto.getMotorbikeFee());
        entity.setOvertimeFee(dto.getOvertimeFee());
        entity.setWaterFee(dto.getWaterFee());
        entity.setElectricityFee(dto.getElectricityFee());
        entity.setDeposit(dto.getDeposit());
        entity.setPayment(dto.getPayment());
        entity.setRentTime(dto.getRentTime());
        entity.setDecorationTime(dto.getDecorationTime());
        entity.setBrokerageFee(dto.getBrokerageFee());
        entity.setNote(dto.getNote());
        entity.setLinkOfBuilding(dto.getLinkOfBuilding());
        entity.setMap(dto.getMap());
        entity.setImage(dto.getImage());
        entity.setManagerName(dto.getManagerName());
        entity.setManagerPhoneNumber(dto.getManagerPhoneNumber());
        entity.setAvatar(dto.getAvatar());
    }
}
