package com.example.buildingmanager.repositories;

import com.example.buildingmanager.entities.ConsignmentRequest;
import com.example.buildingmanager.enums.ConsignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsignmentRepository extends JpaRepository<ConsignmentRequest, Long> {

    // Tìm danh sách theo trạng thái (VD: Tìm tất cả đơn PENDING để duyệt)
    Page<ConsignmentRequest> findByStatus(ConsignmentStatus status, Pageable pageable);

    // Kiểm tra xem SĐT này đã gửi yêu cầu nào chưa xử lý không (Chống spam)
    boolean existsByCustomerPhoneAndStatus(String customerPhone, ConsignmentStatus status);
}