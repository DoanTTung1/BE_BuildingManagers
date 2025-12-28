package com.example.buildingmanager.specifications;

import com.example.buildingmanager.entities.AssignmentBuilding;
import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.Rentarea; // Đã dùng
import com.example.buildingmanager.entities.User; // Đã dùng
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.users.BuildingSearchDTO;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BuildingSpecification {

    // 1. Specification cho ADMIN
    public static Specification<Building> build(BuildingSearchBuilder search) {
        return (root, query, cb) -> {
            // ... (Giữ nguyên hoặc copy logic nếu cần)
            return cb.conjunction();
        };
    }

    // 2. Specification cho USER (Đã cập nhật dùng Rentarea và User)
    public static Specification<Building> build(BuildingSearchDTO search) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (search == null)
                return cb.conjunction();

            // --- 1. TÊN TÒA NHÀ ---
            if (StringUtils.hasText(search.getName())) {
                conditions.add(cb.like(cb.lower(root.get("name")), "%" + search.getName().trim().toLowerCase() + "%"));
            }

            // --- 2. DIỆN TÍCH SÀN (Tổng diện tích sàn) ---
            if (search.getFloorArea() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("floorArea"), search.getFloorArea()));
            }

            // --- 3. QUẬN ---
            if (StringUtils.hasText(search.getDistrict())) {
                conditions.add(cb.like(root.get("district").get("code"), "%" + search.getDistrict() + "%"));
            }

            // --- 4. [MỚI] DIỆN TÍCH THUÊ (Dùng Rentarea) ---
            // Logic: Tìm những tòa nhà có ít nhất 1 diện tích trống nằm trong khoảng user
            // nhập
            if (search.getAreaFrom() != null || search.getAreaTo() != null) {
                // Join bảng Rentarea
                Join<Building, Rentarea> rentAreaJoin = root.join("rentAreas", JoinType.INNER);

                if (search.getAreaFrom() != null) {
                    conditions.add(cb.greaterThanOrEqualTo(rentAreaJoin.get("value"), search.getAreaFrom()));
                }
                if (search.getAreaTo() != null) {
                    conditions.add(cb.lessThanOrEqualTo(rentAreaJoin.get("value"), search.getAreaTo()));
                }
            }

            // --- 5. GIÁ THUÊ ---
            if (search.getRentPriceFrom() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("rentPrice"), search.getRentPriceFrom()));
            }
            if (search.getRentPriceTo() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.get("rentPrice"), search.getRentPriceTo()));
            }

            // --- 6. TÊN QUẢN LÝ ---
            if (StringUtils.hasText(search.getManagerName())) {
                conditions.add(
                        cb.like(cb.lower(root.get("managerName")), "%" + search.getManagerName().toLowerCase() + "%"));
            }

            // --- 7. TÊN NHÂN VIÊN (Dùng User để Join chặt chẽ hơn) ---
            if (StringUtils.hasText(search.getStaffName())) {
                // Join bảng AssignmentBuilding
                Join<Building, AssignmentBuilding> assignmentJoin = root.join("assignmentBuildings", JoinType.INNER);

                // Join tiếp sang bảng User (Staff) - Dùng class User.java ở đây
                Join<AssignmentBuilding, User> staffJoin = assignmentJoin.join("staff", JoinType.INNER);

                // Tìm theo fullName của User
                conditions.add(
                        cb.like(cb.lower(staffJoin.get("fullName")), "%" + search.getStaffName().toLowerCase() + "%"));
            }

            // --- 8. LOẠI TÒA NHÀ ---
            if (search.getTypeCode() != null && !search.getTypeCode().isEmpty()) {
                Join<Object, Object> typeJoin = root.join("rentTypes", JoinType.INNER);
                conditions.add(typeJoin.get("code").in(search.getTypeCode()));
            }

            // DISTINCT: Quan trọng khi dùng JOIN
            query.distinct(true);

            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
}