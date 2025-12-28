package com.example.buildingmanager.repositories;

import com.example.buildingmanager.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Tìm khách hàng theo số điện thoại (Để kiểm tra trùng lặp nếu cần)
    List<Customer> findByPhone(String phone);

    // Tìm tất cả khách hàng chưa bị xóa (Nếu bạn dùng soft delete)
    List<Customer> findByIsActive(Integer isActive);

    // Tìm kiếm theo tên hoặc số điện thoại (Dùng cho Admin tìm khách)
    List<Customer> findByFullNameContainingOrPhoneContaining(String fullName, String phone);
}