package com.example.buildingmanager.controllers;

import com.example.buildingmanager.services.upload.IStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600) // Cho phép ReactJS truy cập
public class FileUploadController {

    private final IStorageService storageService;

    // ========================================================================
    // 1. UPLOAD 1 FILE (Dùng cho Avatar)
    // ========================================================================
    @PostMapping("/image")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // CloudinaryService sẽ trả về link full: "https://res.cloudinary.com/..."
            String url = storageService.storeFile(file);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi upload: " + e.getMessage());
        }
    }

    // ========================================================================
    // 2. UPLOAD NHIỀU FILE (Dùng cho Album ảnh Building)
    // ========================================================================
    @PostMapping("/images")
    public ResponseEntity<List<String>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Cloudinary trả về URL trực tiếp -> Add vào list luôn
                String url = storageService.storeFile(file);
                fileUrls.add(url);
            } catch (Exception e) {
                System.err.println("Lỗi upload file [" + file.getOriginalFilename() + "]: " + e.getMessage());
                // Tiếp tục chạy các file khác chứ không dừng lại
            }
        }
        return ResponseEntity.ok(fileUrls);
    }

    // ========================================================================
    // 3. XÓA FILE (MỚI THÊM - Để xử lý nút X ở Frontend)
    // ========================================================================
    @DeleteMapping("/image")
    public ResponseEntity<?> deleteImage(@RequestParam("url") String url) {
        try {
            // Gọi service để xóa trên Cloudinary
            storageService.deleteFile(url);
            return ResponseEntity.ok("Đã xóa ảnh thành công: " + url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xóa ảnh: " + e.getMessage());
        }
    }

    // ========================================================================
    // 4. XEM FILE (Chỉ dùng nếu bạn lưu file trên ổ cứng Server Local)
    // Nếu dùng Cloudinary thì hàm này KHÔNG CẦN THIẾT, nhưng cứ để đó cũng không
    // sao
    // ========================================================================
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = storageService.loadFileAsResource(fileName);
        if (resource == null)
            return ResponseEntity.notFound().build();

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Không xác định được loại file.");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}