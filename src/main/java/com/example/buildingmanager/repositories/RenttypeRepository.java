package com.example.buildingmanager.repositories;

import com.example.buildingmanager.entities.Renttype;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RenttypeRepository extends JpaRepository<Renttype, Long> {
    // Hàm tìm danh sách Renttype dựa theo danh sách mã Code gửi lên
    List<Renttype> findByCodeIn(List<String> codes);
}