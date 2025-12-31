package com.example.buildingmanager.services.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct; // Import cái này
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Primary
@RequiredArgsConstructor
public class CloudinaryService implements IStorageService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    // Khai báo đối tượng Cloudinary để dùng chung
    private Cloudinary cloudinary;

    // @PostConstruct: Chạy hàm này ngay sau khi Spring tạo xong Service này
    // Giúp chỉ tạo kết nối 1 lần duy nhất -> Tiết kiệm Ram/CPU
    @PostConstruct
    public void initCloudinary() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            // Sửa lỗi gạch vàng: Thêm <String, Object>
            // "resource_type", "auto" -> Tự động nhận diện Ảnh/Video
            Map<String, Object> uploadResult = this.cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "auto"));

            // Trả về đường dẫn URL online
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Lỗi up file lên Cloudinary: " + e.getMessage());
        }
    }

    // --- Các hàm không dùng ---
    @Override
    public void init() {
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        return null;
    }

    @Override
    public void deleteFile(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }

        try {
            // 1. Trích xuất Public ID từ URL
            // VD URL:
            // https://res.cloudinary.com/demo/image/upload/v123456789/anh-toa-nha.jpg
            // -> Cần lấy: "anh-toa-nha"
            String publicId = getPublicIdFromUrl(url);

            if (publicId != null) {
                // 2. Gọi lệnh xóa của Cloudinary
                // ObjectUtils.emptyMap() là tham số config rỗng
                this.cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (IOException e) {
            // Log lỗi ra console nhưng không throw exception để tránh chết luồng chính
            System.err.println("Cảnh báo: Không xóa được ảnh trên Cloudinary. Lỗi: " + e.getMessage());
        }
    }

    /**
     * Hàm phụ trợ: Tách lấy Public ID từ đường dẫn URL
     */
    private String getPublicIdFromUrl(String url) {
        try {
            // Nếu chuỗi truyền vào không phải link http (có thể là ID sẵn) thì trả về luôn
            if (!url.startsWith("http")) {
                return url;
            }

            // Logic cắt chuỗi: Lấy phần text nằm giữa dấu '/' cuối cùng và dấu '.' cuối
            // cùng
            int lastSlashIndex = url.lastIndexOf("/");
            int lastDotIndex = url.lastIndexOf(".");

            if (lastSlashIndex != -1 && lastDotIndex != -1 && lastDotIndex > lastSlashIndex) {
                return url.substring(lastSlashIndex + 1, lastDotIndex);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}