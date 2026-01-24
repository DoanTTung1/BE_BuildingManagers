package com.example.buildingmanager.repositories;

import com.example.buildingmanager.entities.Building;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BuildingRepository extends JpaRepository<Building, Long>, JpaSpecificationExecutor<Building> {

    List<Building> findByCreatedBy(String createdBy);

    // --- CÁC HÀM MỚI CHO DASHBOARD ---

    // 1. Tính tổng doanh thu
    // Lưu ý: CAST(b.rentPrice as double) để tránh lỗi khi rentPrice là Integer
    @Query("SELECT SUM(CAST(b.rentPrice as double)) FROM Building b")
    Double sumTotalRentPrice();

    // 2. Lấy 5 tòa nhà mới nhất (Mới nhất lên đầu)
    List<Building> findTop5ByOrderByIdDesc();

    // 3. Dữ liệu biểu đồ (Gom nhóm theo tháng)
    @Query("SELECT MONTH(b.createdDate) as month, YEAR(b.createdDate) as year, SUM(CAST(b.rentPrice as double)) as total "
            +
            "FROM Building b " +
            "GROUP BY MONTH(b.createdDate), YEAR(b.createdDate) " +
            "ORDER BY YEAR(b.createdDate) ASC, MONTH(b.createdDate) ASC")
    List<Object[]> sumRentPriceGroupedByMonth();

    // Tìm tòa nhà đang Active (status=1) và tên Quận chứa từ khóa (bất kể hoa thường)
    @Query("SELECT b FROM Building b JOIN b.district d WHERE b.status = 1 AND LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Building> findByDistrictName(@Param("keyword") String keyword);
}