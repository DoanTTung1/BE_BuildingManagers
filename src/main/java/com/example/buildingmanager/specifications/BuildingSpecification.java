package com.example.buildingmanager.specifications;

import com.example.buildingmanager.entities.AssignmentBuilding;
import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.Rentarea;
import com.example.buildingmanager.entities.Renttype;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.user.BuildingSearchDTO;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BuildingSpecification {

    // 1. ADMIN (Giữ nguyên)
    public static Specification<Building> build(BuildingSearchBuilder search) {
        return (root, query, cb) -> cb.conjunction();
    }

    // 2. USER (SỬA PHẦN NÀY)
    public static Specification<Building> build(BuildingSearchDTO search) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            if (search == null)
                return cb.conjunction();

            // --- 1. TÊN ---
            if (StringUtils.hasText(search.getName())) {
                conditions.add(cb.like(cb.lower(root.get("name")), "%" + search.getName().trim().toLowerCase() + "%"));
            }

            // --- 2. SÀN ---
            if (search.getFloorArea() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("floorArea"), search.getFloorArea()));
            }

            // --- 3. [SỬA LẠI] QUẬN (QUAN TRỌNG) ---
            // Frontend gửi "QUAN_1". Ta phải join vào District và so sánh cột CODE.
            // Dùng 'equal' thay vì 'like' để chính xác tuyệt đối (tránh tìm QUAN_1 ra
            // QUAN_10)
            if (StringUtils.hasText(search.getDistrict())) {
                // Giả sử Building có quan hệ @ManyToOne với District
                // Và District có field 'code'
                conditions.add(cb.equal(root.get("district").get("code"), search.getDistrict()));
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

            // --- 8. LOẠI TÒA NHÀ ---
            if (search.getTypeCode() != null && !search.getTypeCode().isEmpty()) {
                // 1. Join trực tiếp từ Building sang Renttype
                Join<Building, Renttype> typeJoin = root.join("rentTypes");

                // 2. Tìm các tòa nhà có code nằm trong danh sách gửi lên
                // query.distinct(true) là bắt buộc để tránh ra trùng lặp kết quả
                conditions.add(typeJoin.get("code").in(search.getTypeCode()));
            }
            query.distinct(true);
            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
}