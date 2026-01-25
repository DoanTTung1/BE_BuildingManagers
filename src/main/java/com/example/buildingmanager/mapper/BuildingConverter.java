package com.example.buildingmanager.mapper;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.BuildingImage;
import com.example.buildingmanager.entities.District;
import com.example.buildingmanager.entities.Rentarea;
import com.example.buildingmanager.entities.Renttype;
import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse;
import com.example.buildingmanager.repositories.RenttypeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BuildingConverter {
    private final RenttypeRepository renttypeRepository;

    // --- 1. Dùng cho ADMIN (Search/List) ---
    public BuildingSearchResponse toResponseDTO(Building e) {
        String districName = e.getDistrict() != null ? e.getDistrict().getName() : "";
        String address = e.getStreet() + ", " + e.getWard() + ", " + districName;
        String rentAreaResult = "";

        if (e.getRentAreas() != null && !e.getRentAreas().isEmpty()) {
            rentAreaResult = e.getRentAreas().stream()
                    .map(item -> item.getValue().toString())
                    .collect(Collectors.joining(", "));
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
                .transactionType(e.getTransactionType())
                .build();
    }

    // --- 2. Dùng cho KHÁCH HÀNG (Detail) ---
    public BuildingDetailResponse toDetailResponse(Building e) {
        BuildingDetailResponse dto = new BuildingDetailResponse();
        dto.setId(e.getId());
        dto.setName(e.getName());

        // 🔥 THÊM DÒNG NÀY: Trả về loại giao dịch cho khách biết
        dto.setTransactionType(e.getTransactionType());

        String districtName = "";
        if (e.getDistrict() != null) {
            districtName = e.getDistrict().getName();
        }
        dto.setDistrictName(districtName);
        dto.setAddress(e.getStreet() + ", " + e.getWard() + ", " + districtName);

        dto.setStructure(e.getStructure());
        dto.setNumberOfBasement(e.getNumberOfBasement());
        dto.setFloorArea(e.getFloorArea());
        dto.setDirection(e.getDirection());
        dto.setLevel(e.getLevel());
        dto.setRentPrice(e.getRentPrice());
        dto.setRentPriceDescription(e.getRentPriceDescription());
        dto.setServiceFee(e.getServiceFee());
        dto.setCarFee(e.getCarFee());
        dto.setMotorbikeFee(e.getMotorbikeFee());
        dto.setOvertimeFee(e.getOvertimeFee());
        dto.setWaterFee(e.getWaterFee());
        dto.setElectricityFee(e.getElectricityFee());
        dto.setDeposit(e.getDeposit());
        dto.setPayment(e.getPayment());
        dto.setRentTime(e.getRentTime());
        dto.setDecorationTime(e.getDecorationTime());
        dto.setBrokerageFee(e.getBrokerageFee());
        dto.setNote(e.getNote());
        dto.setLinkOfBuilding(e.getLinkOfBuilding());
        dto.setMap(e.getMap());
        dto.setImage(e.getAvatar());

        List<String> albumImages = new ArrayList<>();
        if (e.getBuildingImages() != null) {
            albumImages = e.getBuildingImages().stream()
                    .map(BuildingImage::getLink)
                    .collect(Collectors.toList());
        }
        dto.setImageList(albumImages);

        dto.setManagerName(e.getManagerName());
        dto.setManagerPhoneNumber(e.getManagerPhoneNumber());

        List<Rentarea> rentAreas = e.getRentAreas();
        if (rentAreas != null && !rentAreas.isEmpty()) {
            String areaString = rentAreas.stream()
                    .map(item -> item.getValue().toString())
                    .collect(Collectors.joining(", "));
            dto.setRentAreaResult(areaString + " m2");
        }

        return dto;
    }

    // --- 3. Dùng cho ADMIN (Create) ---
    public Building toEntity(UpdateAndCreateBuildingDTO dto) {
        District district = null;
        if (dto.getDistrictId() != null) {
            district = new District();
            district.setId(dto.getDistrictId());
        }

        Building building = Building.builder()
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
                .managerName(dto.getManagerName())
                .managerPhoneNumber(dto.getManagerPhoneNumber())
                .avatar(dto.getImage())
                .transactionType(dto.getTransactionType())
                .build();

        if (dto.getImageList() != null && !dto.getImageList().isEmpty()) {
            List<BuildingImage> buildingImages = new ArrayList<>();
            for (String url : dto.getImageList()) {
                BuildingImage img = new BuildingImage();
                img.setLink(url);
                img.setBuilding(building);
                buildingImages.add(img);
            }
            building.setBuildingImages(buildingImages);
        }

        if (dto.getTypeCode() != null && !dto.getTypeCode().isEmpty()) {
            List<Renttype> rentTypes = renttypeRepository.findByCodeIn(dto.getTypeCode());
            building.setRentTypes(rentTypes);
        }

        return building;
    }

    // --- 4. Dùng cho ADMIN (Load dữ liệu lên form sửa) ---
    public UpdateAndCreateBuildingDTO toDTO(Building entity) {
        List<String> imgList = new ArrayList<>();
        if (entity.getBuildingImages() != null) {
            imgList = entity.getBuildingImages().stream()
                    .map(BuildingImage::getLink)
                    .collect(Collectors.toList());
        }

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
                .managerName(entity.getManagerName())
                .managerPhoneNumber(entity.getManagerPhoneNumber())
                .image(entity.getAvatar())
                .imageList(imgList)
                .transactionType(entity.getTransactionType())
                .build();
    }

    // --- 5. Hàm update Entity ---
    // --- 5. Hàm update Entity (PHIÊN BẢN UPDATE THÔNG MINH - FIX LỖI NULL) ---
    public void updateEntity(UpdateAndCreateBuildingDTO dto, Building entity) {
        // 1. Cập nhật Quận
        if (dto.getDistrictId() != null) {
            District d = new District();
            d.setId(dto.getDistrictId());
            entity.setDistrict(d);
        }

        // 2. Cập nhật các trường String (Chỉ update nếu có gửi lên)
        if (dto.getName() != null)
            entity.setName(dto.getName());
        if (dto.getStreet() != null)
            entity.setStreet(dto.getStreet());
        if (dto.getWard() != null)
            entity.setWard(dto.getWard());
        if (dto.getStructure() != null)
            entity.setStructure(dto.getStructure());
        if (dto.getDirection() != null)
            entity.setDirection(dto.getDirection());
        if (dto.getLevel() != null)
            entity.setLevel(dto.getLevel());
        if (dto.getRentPriceDescription() != null)
            entity.setRentPriceDescription(dto.getRentPriceDescription());
        if (dto.getServiceFee() != null)
            entity.setServiceFee(dto.getServiceFee());
        if (dto.getCarFee() != null)
            entity.setCarFee(dto.getCarFee());
        if (dto.getMotorbikeFee() != null)
            entity.setMotorbikeFee(dto.getMotorbikeFee());
        if (dto.getOvertimeFee() != null)
            entity.setOvertimeFee(dto.getOvertimeFee());
        if (dto.getWaterFee() != null)
            entity.setWaterFee(dto.getWaterFee());
        if (dto.getElectricityFee() != null)
            entity.setElectricityFee(dto.getElectricityFee());
        if (dto.getDeposit() != null)
            entity.setDeposit(dto.getDeposit());
        if (dto.getPayment() != null)
            entity.setPayment(dto.getPayment());
        if (dto.getRentTime() != null)
            entity.setRentTime(dto.getRentTime());
        if (dto.getDecorationTime() != null)
            entity.setDecorationTime(dto.getDecorationTime());
        if (dto.getNote() != null)
            entity.setNote(dto.getNote());
        if (dto.getLinkOfBuilding() != null)
            entity.setLinkOfBuilding(dto.getLinkOfBuilding());
        if (dto.getMap() != null)
            entity.setMap(dto.getMap());
        if (dto.getManagerName() != null)
            entity.setManagerName(dto.getManagerName());
        if (dto.getManagerPhoneNumber() != null)
            entity.setManagerPhoneNumber(dto.getManagerPhoneNumber());
        if (dto.getImage() != null)
            entity.setAvatar(dto.getImage());

        // 3. Cập nhật các trường Số (Quan trọng: check null để tránh lỗi DB)
        if (dto.getNumberOfBasement() != null)
            entity.setNumberOfBasement(dto.getNumberOfBasement());
        if (dto.getFloorArea() != null)
            entity.setFloorArea(dto.getFloorArea());

        // 👇👇👇 ĐÂY LÀ CHỖ FIX LỖI CỦA BẠN 👇👇👇
        if (dto.getRentPrice() != null)
            entity.setRentPrice(dto.getRentPrice());

        if (dto.getBrokerageFee() != null)
            entity.setBrokerageFee(dto.getBrokerageFee());

        // 4. Cập nhật Loại giao dịch
        if (dto.getTransactionType() != null)
            entity.setTransactionType(dto.getTransactionType());

        // 5. Cập nhật Loại tòa nhà (Rent Types)
        if (dto.getTypeCode() != null && !dto.getTypeCode().isEmpty()) {
            List<Renttype> rentTypes = renttypeRepository.findByCodeIn(dto.getTypeCode());
            entity.setRentTypes(rentTypes);
        }

        // 6. Cập nhật Album ảnh
        if (dto.getImageList() != null) {
            if (entity.getBuildingImages() == null) {
                entity.setBuildingImages(new ArrayList<>());
            } else {
                entity.getBuildingImages().clear();
            }

            for (String url : dto.getImageList()) {
                BuildingImage img = new BuildingImage();
                img.setLink(url);
                img.setBuilding(entity);
                entity.getBuildingImages().add(img);
            }
        }
    }
}