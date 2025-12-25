package com.example.buildingmanager.specifications;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.Rentarea;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.users.BuildingSearchDTO;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BuildingSpecification {

    public static Specification<Building> build(BuildingSearchBuilder search) {
        return (Root<Building> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (search == null) return cb.conjunction();

            // 1. TÊN TÒA NHÀ (Giữ nguyên - Tốt)
            if (search.getName() != null && !search.getName().trim().isEmpty()) {
                conditions.add(cb.like(cb.lower(root.get("name")), "%" + search.getName().trim().toLowerCase() + "%"));
            }

            // 2. DIỆN TÍCH SÀN (SỬA: Dùng >= thay vì =)
            if (search.getFloorArea() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("floorArea"), search.getFloorArea()));
            }

            // 3. QUẬN (Giữ nguyên hoặc nâng cấp lên IN nếu cần)
            if (search.getDistrictId() != null) {
                conditions.add(cb.equal(root.get("district").get("id"), search.getDistrictId()));
            }

            // 4. DIỆN TÍCH THUÊ (Giữ nguyên - Tốt)
            if (search.getAreaFrom() != null || search.getAreaTo() != null) {
                Join<Building, Rentarea> rentAreaJoin = root.join("rentAreas", JoinType.INNER);
                
                if (search.getAreaFrom() != null) {
                    conditions.add(cb.greaterThanOrEqualTo(rentAreaJoin.get("value"), search.getAreaFrom()));
                }
                if (search.getAreaTo() != null) {
                    conditions.add(cb.lessThanOrEqualTo(rentAreaJoin.get("value"), search.getAreaTo()));
                }
            }

            // 5. GIÁ THUÊ (Giữ nguyên - Tốt)
            if (search.getRentPriceFrom() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("rentPrice"), search.getRentPriceFrom()));
            }
            if (search.getRentPriceTo() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.get("rentPrice"), search.getRentPriceTo()));
            }

            // 6. NHÂN VIÊN PHỤ TRÁCH (Giữ nguyên - Tốt)
            if (search.getStaffId() != null) {
                Join<Object, Object> assignmentJoin = root.join("assignmentBuildings", JoinType.INNER);
                conditions.add(cb.equal(assignmentJoin.get("staff").get("id"), search.getStaffId()));
            }

            // 7. [MỚI] LOẠI TÒA NHÀ (Type Code) - BỔ SUNG THÊM CÁI NÀY
            // Giả sử BuildingSearchBuilder có List<String> typeCode
            // Và Building entity có quan hệ hoặc trường type
            if (search.getTypeCode() != null && !search.getTypeCode().isEmpty()) {
                // Cách 1: Nếu type là String trong bảng Building (VD: type="TANG_TRET")
                // conditions.add(root.get("type").in(search.getTypeCode()));
                
                // Cách 2: Nếu type lưu trong bảng riêng (VD: BuildingRentType) -> Cần JOIN
                // Join<Building, Map<String, Object>> typeJoin = root.join("rentTypes", JoinType.INNER); // Ví dụ
                // conditions.add(typeJoin.get("code").in(search.getTypeCode()));
            }

            // DISTINCT (Quan trọng - Giữ nguyên)
            query.distinct(true);

            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
    public static Specification<Building> build(BuildingSearchDTO search) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (search == null) return cb.conjunction();

            // 1. TÊN TÒA NHÀ
            if (search.getName() != null && !search.getName().trim().isEmpty()) {
                conditions.add(cb.like(cb.lower(root.get("name")), "%" + search.getName().toLowerCase() + "%"));
            }

            // 2. DIỆN TÍCH SÀN (User thường tìm >=)
            if (search.getFloorArea() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("floorArea"), search.getFloorArea()));
            }

            // 3. QUẬN (Kiểm tra lại DTO của bạn là String hay ID)
            if (search.getDistrict() != null && !search.getDistrict().isEmpty()) {
                // Nếu DTO User gửi lên mã quận (String) thì dùng cái này:
                 conditions.add(cb.like(root.get("district").get("code"), "%" + search.getDistrict() + "%"));
            }

            // 4. GIÁ THUÊ TỪ
            if (search.getRentPriceFrom() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("rentPrice"), search.getRentPriceFrom()));
            }

            // 5. ĐẾN GIÁ THUÊ
            if (search.getRentPriceTo() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.get("rentPrice"), search.getRentPriceTo()));
            }

            // 6. DIỆN TÍCH THUÊ (Nếu User có tìm cái này)
            // (Bạn copy đoạn Join RentArea từ trên xuống nếu cần)
            
            // DISTINCT để tránh trùng lặp
            query.distinct(true);

            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
}

