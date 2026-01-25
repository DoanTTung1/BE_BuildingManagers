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
    // 1. ADMIN SEARCH (Dành cho trang quản trị)
    // =========================================================================
    public static Specification<Building> build(BuildingSearchBuilder search) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (search == null)
                return cb.conjunction();

            // 1. Tên (Gần đúng)
            if (StringUtils.hasText(search.getName())) {
                conditions.add(cb.like(cb.lower(root.get("name")), "%" + search.getName().trim().toLowerCase() + "%"));
            }

            // 2. Diện tích sàn
            if (search.getFloorArea() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("floorArea"), search.getFloorArea()));
            }

            // 3. Quận (Theo ID)
            if (search.getDistrictId() != null) {
                Join<Building, District> districtJoin = root.join("district", JoinType.INNER);
                conditions.add(cb.equal(districtJoin.get("id"), search.getDistrictId()));
            }

            // 4. Giá thuê (Khoảng giá)
            if (search.getRentPriceFrom() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("rentPrice"), search.getRentPriceFrom()));
            }
            if (search.getRentPriceTo() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.get("rentPrice"), search.getRentPriceTo()));
            }

            // 5. Nhân viên phụ trách
            if (search.getStaffId() != null) {
                Join<Building, AssignmentBuilding> assignmentJoin = root.join("assignmentBuildings", JoinType.INNER);
                conditions.add(cb.equal(assignmentJoin.get("staff").get("id"), search.getStaffId()));
            }

            // 6. Loại hình (Nội thất, Tầng trệt...)
            if (search.getTypeCode() != null && !search.getTypeCode().isEmpty()) {
                Join<Building, Renttype> rentTypeJoin = root.join("rentTypes", JoinType.INNER);
                conditions.add(rentTypeJoin.get("code").in(search.getTypeCode()));
            }

            // 7. Status (Trạng thái)
            if (search.getStatus() != null) {
                conditions.add(cb.equal(root.get("status"), search.getStatus()));
            } else {
                conditions.add(cb.notEqual(root.get("status"), 0)); // Mặc định ẩn bài đã xóa
            }

            // 8. 🔥 [MỚI] TRANSACTION TYPE (MUA / THUÊ)
            if (StringUtils.hasText(search.getTransactionType())) {
                conditions.add(cb.equal(root.get("transactionType"), search.getTransactionType()));
            }

            // 9. 🔥 [MỚI] SORTING (SẮP XẾP)
            if (StringUtils.hasText(search.getSortBy())) {
                switch (search.getSortBy()) {
                    case "price_asc":
                        query.orderBy(cb.asc(root.get("rentPrice")));
                        break;
                    case "price_desc":
                        query.orderBy(cb.desc(root.get("rentPrice")));
                        break;
                    default: // "newest"
                        query.orderBy(cb.desc(root.get("id")));
                        break;
                }
            } else {
                query.orderBy(cb.desc(root.get("id"))); // Mặc định mới nhất
            }

            query.distinct(true);
            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }

    // =========================================================================
    // 2. USER SEARCH (Dành cho trang chủ - Khách hàng)
    // =========================================================================
    public static Specification<Building> build(BuildingSearchDTO search) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (search == null)
                return cb.conjunction();

            // 1. Tên
            if (StringUtils.hasText(search.getName())) {
                conditions.add(cb.like(cb.lower(root.get("name")), "%" + search.getName().trim().toLowerCase() + "%"));
            }

            // 2. Diện tích sàn
            if (search.getFloorArea() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("floorArea"), search.getFloorArea()));
            }

            // 3. Quận (Theo ID - Đã Fix)
            if (search.getDistrictId() != null) {
                Join<Building, District> districtJoin = root.join("district", JoinType.INNER);
                conditions.add(cb.equal(districtJoin.get("id"), search.getDistrictId()));
            }

            // 4. Diện tích thuê (Rent Area - Bảng con)
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

            // 7. Nhân viên phụ trách
            if (StringUtils.hasText(search.getStaffName())) {
                Join<Building, AssignmentBuilding> assignmentJoin = root.join("assignmentBuildings", JoinType.INNER);
                Join<AssignmentBuilding, User> staffJoin = assignmentJoin.join("staff", JoinType.INNER);
                conditions.add(
                        cb.like(cb.lower(staffJoin.get("fullName")), "%" + search.getStaffName().toLowerCase() + "%"));
            }

            // 8. Loại hình (Nội thất, Nguyên căn...)
            if (search.getTypeCode() != null && !search.getTypeCode().isEmpty()) {
                Join<Building, Renttype> rentTypeJoin = root.join("rentTypes", JoinType.INNER);
                conditions.add(rentTypeJoin.get("code").in(search.getTypeCode()));
            }

            // 9. 🔥 [MỚI] TRANSACTION TYPE (MUA / THUÊ)
            if (StringUtils.hasText(search.getTransactionType())) {
                conditions.add(cb.equal(root.get("transactionType"), search.getTransactionType()));
            }

            // 10. BẮT BUỘC: Status = 1 (Active)
            conditions.add(cb.equal(root.get("status"), 1));

            // 11. 🔥 [MỚI] SORTING (SẮP XẾP)
            if (StringUtils.hasText(search.getSortBy())) {
                switch (search.getSortBy()) {
                    case "price_asc":
                        query.orderBy(cb.asc(root.get("rentPrice")));
                        break;
                    case "price_desc":
                        query.orderBy(cb.desc(root.get("rentPrice")));
                        break;
                    default: // "newest"
                        query.orderBy(cb.desc(root.get("id")));
                        break;
                }
            } else {
                query.orderBy(cb.desc(root.get("id"))); // Mặc định mới nhất
            }

            query.distinct(true);
            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
}