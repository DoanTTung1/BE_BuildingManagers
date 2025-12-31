package com.example.buildingmanager.mapper;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.BuildingImage;
import com.example.buildingmanager.entities.District;
import com.example.buildingmanager.entities.Rentarea;
import com.example.buildingmanager.models.admin.UpdateAndCreateBuildingDTO;
import com.example.buildingmanager.models.admin.response.BuildingSearchResponse;
import com.example.buildingmanager.models.building.BuildingDetailResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BuildingConverter {

    // --- 1. Dùng cho ADMIN (Search/List) ---
    public BuildingSearchResponse toResponseDTO(Building e) {
        String districName = e.getDistrict() != null ? e.getDistrict().getName() : "";
        String address = e.getStreet() + ", " + e.getWard() + ", " + districName;
        String rentAreaResult = "";

        // Logic nối chuỗi diện tích thuê (100, 200 m2)
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
                .avatar(e.getAvatar()) // Chỉ lấy ảnh đại diện
                .brokerageFee((e.getBrokerageFee() != null ? e.getBrokerageFee().toString() : ""))
                .build();
    }

    // --- 2. Dùng cho KHÁCH HÀNG (Xem chi tiết - Detail) ---
    public BuildingDetailResponse toDetailResponse(Building e) {
        BuildingDetailResponse dto = new BuildingDetailResponse();
        dto.setId(e.getId());
        dto.setName(e.getName());

        // Xử lý Quận và Địa chỉ
        String districtName = "";
        if (e.getDistrict() != null) {
            districtName = e.getDistrict().getName();
        }
        dto.setDistrictName(districtName);
        dto.setAddress(e.getStreet() + ", " + e.getWard() + ", " + districtName);

        // Map các trường thông tin
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
        // (Nếu DTO detail có trường avatar thì set, không thì thôi)

        // Lấy danh sách Album ảnh trả về cho khách xem
        List<String> albumImages = new ArrayList<>();
        if (e.getBuildingImages() != null) {
            albumImages = e.getBuildingImages().stream()
                    .map(BuildingImage::getLink) // Lấy đường dẫn link
                    .collect(Collectors.toList());
        }
        // Lưu ý: Bạn cần thêm field `private List<String> imageList;` vào
        // BuildingDetailResponse
        dto.setImageList(albumImages);

        // Thông tin quản lý
        dto.setManagerName(e.getManagerName());
        dto.setManagerPhoneNumber(e.getManagerPhoneNumber());

        // Xử lý RentArea hiển thị
        List<Rentarea> rentAreas = e.getRentAreas();
        if (rentAreas != null && !rentAreas.isEmpty()) {
            String areaString = rentAreas.stream()
                    .map(item -> item.getValue().toString())
                    .collect(Collectors.joining(", "));
            dto.setRentAreaResult(areaString + " m2");
        }

        return dto;
    }

    // --- 3. Dùng cho ADMIN (Create - Chuyển DTO vào Entity) ---
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
                // .image(dto.getImage()) --> XÓA DÒNG NÀY
                .managerName(dto.getManagerName())
                .managerPhoneNumber(dto.getManagerPhoneNumber())
                .avatar(dto.getAvatar()) // Set Avatar riêng
                .build();

        // --- XỬ LÝ ALBUM ẢNH (New) ---
        if (dto.getImageList() != null && !dto.getImageList().isEmpty()) {
            List<BuildingImage> buildingImages = new ArrayList<>();
            for (String url : dto.getImageList()) {
                BuildingImage img = new BuildingImage();
                img.setLink(url);
                img.setBuilding(building); // Quan trọng: Gắn ảnh vào tòa nhà
                buildingImages.add(img);
            }
            building.setBuildingImages(buildingImages);
        }

        return building;
    }

    // --- 4. Dùng cho ADMIN (Load dữ liệu cũ lên form sửa) ---
    public UpdateAndCreateBuildingDTO toDTO(Building entity) {

        // Lấy danh sách link ảnh từ Entity để đổ lên Form
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
                // .image(entity.getImage()) --> XÓA
                .managerName(entity.getManagerName())
                .managerPhoneNumber(entity.getManagerPhoneNumber())
                .avatar(entity.getAvatar())
                .imageList(imgList) // Set list ảnh vào DTO
                .build();
    }

    // --- 5. Hàm update Entity ---
    public void updateEntity(UpdateAndCreateBuildingDTO dto, Building entity) {
        if (dto.getDistrictId() != null) {
            District d = new District();
            d.setId(dto.getDistrictId());
            entity.setDistrict(d);
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
        // entity.setImage(dto.getImage()); --> XÓA
        entity.setManagerName(dto.getManagerName());
        entity.setManagerPhoneNumber(dto.getManagerPhoneNumber());
        entity.setAvatar(dto.getAvatar());

        // --- XỬ LÝ UPDATE ALBUM ẢNH ---
        // Logic: Xóa cũ -> Thêm mới (Hoặc để Service xử lý việc xóa, ở đây chỉ tạo list
        // mới)
        if (dto.getImageList() != null) {
            // Xóa list cũ trong object Java (Hibernate sẽ tự xử lý DB nếu có
            // orphanRemoval=true)
            entity.getBuildingImages().clear();

            for (String url : dto.getImageList()) {
                BuildingImage img = new BuildingImage();
                img.setLink(url);
                img.setBuilding(entity); // Gắn vào entity hiện tại
                entity.getBuildingImages().add(img);
            }
        }
    }
}