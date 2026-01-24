package com.example.buildingmanager.specifications;

import com.example.buildingmanager.entities.*;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import com.example.buildingmanager.models.user.BuildingSearchDTO;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BuildingSpecification {

    // =========================================================================
    // 1. ADMIN SEARCH (Full quyền + lọc thùng rác) - ĐÃ CHUẨN
    // =========================================================================
    public static Specification<Building> build(BuildingSearchBuilder search) {

        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (search == null) {
                return cb.conjunction();
            }

            // --- 1. TÊN TÒA NHÀ ---
            if (StringUtils.hasText(search.getName())) {
                conditions.add(cb.like(cb.lower(root.get("name")), "%" + search.getName().trim().toLowerCase() + "%"));
            }

            // --- 2. DIỆN TÍCH SÀN ---
            if (search.getFloorArea() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("floorArea"), search.getFloorArea()));
            }

            // --- 3. QUẬN (ĐÃ FIX: Lọc theo ID) ---
            if (search.getDistrictId() != null) {
                Join<Building, District> districtJoin = root.join("district", JoinType.INNER);
                conditions.add(cb.equal(districtJoin.get("id"), search.getDistrictId()));
            }

            // --- 4. GIÁ THUÊ ---
            if (search.getRentPriceFrom() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("rentPrice"), search.getRentPriceFrom()));
            }
            if (search.getRentPriceTo() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.get("rentPrice"), search.getRentPriceTo()));
            }

            // --- 5. NHÂN VIÊN PHỤ TRÁCH ---
            if (search.getStaffId() != null) {
                Join<Building, AssignmentBuilding> assignmentJoin = root.join("assignmentBuildings", JoinType.INNER);
                conditions.add(cb.equal(assignmentJoin.get("staff").get("id"), search.getStaffId()));
            }

            // --- 6. LOẠI TÒA NHÀ ---
            if (search.getTypeCode() != null && !search.getTypeCode().isEmpty()) {
                Join<Building, Renttype> rentTypeJoin = root.join("rentTypes", JoinType.INNER);
                conditions.add(rentTypeJoin.get("code").in(search.getTypeCode()));
            }

            // --- 7. STATUS ---
            if (search.getStatus() != null) {
                conditions.add(cb.equal(root.get("status"), search.getStatus()));
            } else {
                // Mặc định không lấy bài đã xóa (status 0) nếu không chọn status
                conditions.add(cb.notEqual(root.get("status"), 0));
            }

            query.distinct(true);
            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }

    // =========================================================================
    // 2. USER SEARCH (Công khai – chỉ xem ACTIVE) - ĐÃ SỬA LẠI CHO ĐÚNG
    // =========================================================================
    public static Specification<Building> build(BuildingSearchDTO search) {

        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (search == null) {
                return cb.conjunction();
            }

            // 1. Tên
            if (StringUtils.hasText(search.getName())) {
                conditions.add(cb.like(cb.lower(root.get("name")), "%" + search.getName().trim().toLowerCase() + "%"));
            }

            // 2. Diện tích sàn
            if (search.getFloorArea() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("floorArea"), search.getFloorArea()));
            }

            // 3. QUẬN (🔥 ĐÃ SỬA: Đổi từ Code String sang ID Long để khớp Frontend)
            if (search.getDistrictId() != null) {
                Join<Building, District> districtJoin = root.join("district", JoinType.INNER);
                conditions.add(cb.equal(districtJoin.get("id"), search.getDistrictId()));
            }

            // 4. Diện tích thuê (Rent Area)
            if (search.getAreaFrom() != null || search.getAreaTo() != null) {
                Join<Building, Rentarea> rentAreaJoin = root.join("rentAreas", JoinType.INNER);

                if (search.getAreaFrom() != null) {
                    conditions.add(cb.greaterThanOrEqualTo(rentAreaJoin.get("value"), search.getAreaFrom()));
                }
                if (search.getAreaTo() != null) {
                    conditions.add(cb.lessThanOrEqualTo(rentAreaJoin.get("value"), search.getAreaTo()));
                }
            }

            // 5. Giá thuê
            if (search.getRentPriceFrom() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("rentPrice"), search.getRentPriceFrom()));
            }
            if (search.getRentPriceTo() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.get("rentPrice"), search.getRentPriceTo()));
            }

            // 6. Tên quản lý
            if (StringUtils.hasText(search.getManagerName())) {
                conditions.add(
                        cb.like(cb.lower(root.get("managerName")), "%" + search.getManagerName().toLowerCase() + "%"));
            }

            // 7. Tên nhân viên (nếu cần)
            if (StringUtils.hasText(search.getStaffName())) {
                Join<Building, AssignmentBuilding> assignmentJoin = root.join("assignmentBuildings", JoinType.INNER);
                Join<AssignmentBuilding, User> staffJoin = assignmentJoin.join("staff", JoinType.INNER);
                conditions.add(
                        cb.like(cb.lower(staffJoin.get("fullName")), "%" + search.getStaffName().toLowerCase() + "%"));
            }

            // 8. Loại tòa nhà
            if (search.getTypeCode() != null && !search.getTypeCode().isEmpty()) {
                Join<Building, Renttype> rentTypeJoin = root.join("rentTypes", JoinType.INNER);
                conditions.add(rentTypeJoin.get("code").in(search.getTypeCode()));
            }

            // 9. BẮT BUỘC: Chỉ lấy bài đang ACTIVE (Status = 1)
            conditions.add(cb.equal(root.get("status"), 1));

            query.distinct(true);
            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
}