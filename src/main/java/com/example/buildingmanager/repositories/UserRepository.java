package com.example.buildingmanager.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.buildingmanager.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserNameAndStatus(String userName, Integer status);

    // Tìm user theo Role và Status (Ví dụ: Lấy tất cả STAFF đang hoạt động)
   List<User> findByRoles_CodeAndStatus(String roleCode, Integer status);

    // Tìm tất cả user theo Role (Kể cả bị khóa)
    List<User> findByRoles_Code(String roleCode);

    // Kiểm tra trùng username
    boolean existsByUserName(String userName);
}