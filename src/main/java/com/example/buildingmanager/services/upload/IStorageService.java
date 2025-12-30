package com.example.buildingmanager.services.upload;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    // Hàm khởi tạo thư mục lưu trữ
    void init();

    // Hàm lưu 1 file -> Trả về tên file đã lưu
    String storeFile(MultipartFile file);

    // Hàm load file ra để xem (dành cho API GET)
    Resource loadFileAsResource(String fileName);
    
    // Hàm xóa file (nếu cần)
    void deleteFile(String fileName);
}