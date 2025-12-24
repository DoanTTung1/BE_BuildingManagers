package com.example.buildingmanager.specifications;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.entities.Rentarea;
import com.example.buildingmanager.models.admin.request.BuildingSearchBuilder;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class BuildingSpecification {
    public static Specification<Building> build (BuildingSearchBuilder search)
    {
        return (Root<Building> root, CriteriaQuery<?>query, CriteriaBuilder cb)->
        {
            List<Predicate> conditions = new ArrayList<>();

            if(search==null) return cb.conjunction();
            // =========================
            // 2️⃣ TÌM THEO TÊN TÒA NHÀ (LIKE)
            // =========================
            if (search.getName() != null && !search.getName().trim().isEmpty()) {

                String keyword = search.getName().trim().toLowerCase();

                conditions.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + keyword + "%"
                        )
                );
            }

            // =========================
            // 3️⃣ TÌM THEO DIỆN TÍCH SÀN (EQUAL)
            // =========================
            if (search.getFloorArea() != null) {
                conditions.add(
                        cb.equal(
                                root.get("floorArea"),
                                search.getFloorArea()
                        )
                );
            }

            // =========================
            // 4️⃣ TÌM THEO QUẬN (JOIN District)
            // =========================
            if (search.getDistrictId() != null) {
                conditions.add(
                        cb.equal(
                                root.get("district").get("id"),
                                search.getDistrictId()
                        )
                );
            }

            // =========================
            // 5️⃣ TÌM THEO KHOẢNG DIỆN TÍCH THUÊ
            // (JOIN bảng RentArea – quan hệ 1-N)
            // =========================
            if (search.getAreaFrom() != null || search.getAreaTo() != null) {

                Join<Building, Rentarea> rentAreaJoin =
                        root.join("rentAreas", JoinType.INNER);

                if (search.getAreaFrom() != null) {
                    conditions.add(
                            cb.greaterThanOrEqualTo(
                                    rentAreaJoin.get("value"),
                                    search.getAreaFrom()
                            )
                    );
                }

                if (search.getAreaTo() != null) {
                    conditions.add(
                            cb.lessThanOrEqualTo(
                                    rentAreaJoin.get("value"),
                                    search.getAreaTo()
                            )
                    );
                }
            }

            // =========================
            // 6️⃣ TÌM THEO KHOẢNG GIÁ THUÊ
            // =========================
            if (search.getRentPriceFrom() != null) {
                conditions.add(
                        cb.greaterThanOrEqualTo(
                                root.get("rentPrice"),
                                search.getRentPriceFrom()
                        )
                );
            }

            if (search.getRentPriceTo() != null) {
                conditions.add(
                        cb.lessThanOrEqualTo(
                                root.get("rentPrice"),
                                search.getRentPriceTo()
                        )
                );
            }

            // =========================
            // 7️⃣ TÌM THEO NHÂN VIÊN PHỤ TRÁCH
            // (JOIN AssignmentBuilding)
            // =========================
            if (search.getStaffId() != null) {

                Join<?, ?> assignmentJoin =
                        root.join("assignmentBuildings", JoinType.INNER);

                conditions.add(
                        cb.equal(
                                assignmentJoin.get("staff").get("id"),
                                search.getStaffId()
                        )
                );
            }

            // =========================
            // 8️⃣ TRÁNH TRÙNG KẾT QUẢ
            // (do JOIN bảng 1-N)
            // =========================
            query.distinct(true);

            // =========================
            // 9️⃣ GỘP TẤT CẢ ĐIỀU KIỆN (AND)
            // =========================
            return cb.and(conditions.toArray(new Predicate[0]));
        };
        }
    }

