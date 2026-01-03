package com.example.buildingmanager.specifications;

import com.example.buildingmanager.entities.AssignmentBuilding;
import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.Rentarea;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.user.BuildingSearchDTO;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BuildingSpecification {

    // 1. ADMIN (Giữ nguyên hoặc copy logic dưới lên)
    public static Specification<Building> build(BuildingSearchBuilder search) {
        return (root, query, cb) -> cb.conjunction();
    }

    // 2. USER
    public static Specification<Building> build(BuildingSearchDTO search) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (search == null)
                return cb.conjunction();

            // ... (Các điều kiện 1 -> 7 giữ nguyên như cũ) ...

            // --- 1. TÊN ---
            if (StringUtils.hasText(search.getName())) {
                conditions.add(cb.like(cb.lower(root.get("name")), "%" + search.getName().trim().toLowerCase() + "%"));
            }
            // --- 2. SÀN ---
            if (search.getFloorArea() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("floorArea"), search.getFloorArea()));
            }
            // --- 3. QUẬN ---
            if (StringUtils.hasText(search.getDistrict())) {
                conditions.add(cb.like(root.get("district").get("code"), "%" + search.getDistrict() + "%"));
            }
            // --- 4. DIỆN TÍCH THUÊ ---
            if (search.getAreaFrom() != null || search.getAreaTo() != null) {
                Join<Building, Rentarea> rentAreaJoin = root.join("rentAreas", JoinType.INNER);
                if (search.getAreaFrom() != null)
                    conditions.add(cb.greaterThanOrEqualTo(rentAreaJoin.get("value"), search.getAreaFrom()));
                if (search.getAreaTo() != null)
                    conditions.add(cb.lessThanOrEqualTo(rentAreaJoin.get("value"), search.getAreaTo()));
            }
            // --- 5. GIÁ ---
            if (search.getRentPriceFrom() != null)
                conditions.add(cb.greaterThanOrEqualTo(root.get("rentPrice"), search.getRentPriceFrom()));
            if (search.getRentPriceTo() != null)
                conditions.add(cb.lessThanOrEqualTo(root.get("rentPrice"), search.getRentPriceTo()));

            // --- 6. QUẢN LÝ ---
            if (StringUtils.hasText(search.getManagerName())) {
                conditions.add(
                        cb.like(cb.lower(root.get("managerName")), "%" + search.getManagerName().toLowerCase() + "%"));
            }
            // --- 7. NHÂN VIÊN ---
            if (StringUtils.hasText(search.getStaffName())) {
                Join<Building, AssignmentBuilding> assignmentJoin = root.join("assignmentBuildings", JoinType.INNER);
                Join<AssignmentBuilding, User> staffJoin = assignmentJoin.join("staff", JoinType.INNER);
                conditions.add(
                        cb.like(cb.lower(staffJoin.get("fullName")), "%" + search.getStaffName().toLowerCase() + "%"));
            }

            // --- 8. [ĐÃ SỬA] LOẠI TÒA NHÀ ---
            // Logic: Vì lưu dạng chuỗi "TANG_TRET,NGUYEN_CAN", ta dùng OR LIKE để tìm
            if (search.getTypeCode() != null && !search.getTypeCode().isEmpty()) {
                List<Predicate> typePredicates = new ArrayList<>();
                for (String code : search.getTypeCode()) {
                    // Tìm xem chuỗi type có chứa code này không
                    typePredicates.add(cb.like(root.get("type"), "%" + code + "%"));
                }
                // Dùng OR (chỉ cần thỏa mãn 1 trong các loại chọn là được)
                conditions.add(cb.or(typePredicates.toArray(new Predicate[0])));
            }

            query.distinct(true);
            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
}