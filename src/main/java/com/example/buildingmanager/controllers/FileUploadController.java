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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    // 1. UPLOAD 1 FILE (Dùng cho Avatar, hoặc upload lẻ)
    // ========================================================================
    @PostMapping("/image")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Gọi Service (Bây giờ nó là CloudinaryService)
            // Nó trả về luôn link: "https://res.cloudinary.com/..."
            String url = storageService.storeFile(file);

            // Trả về luôn cho Frontend
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
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
                // Lưu từng file
                String fileName = storageService.storeFile(file);

                // Tạo URL
                String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/upload/files/")
                        .path(fileName)
                        .toUriString();

                fileUrls.add(fileUrl);

            } catch (Exception e) {
                // Nếu lỗi 1 file thì log ra console, vẫn tiếp tục các file khác
                System.err.println("Lỗi upload file [" + file.getOriginalFilename() + "]: " + e.getMessage());
            }
        }
        return ResponseEntity.ok(fileUrls);
    }

    // ========================================================================
    // 3. XEM FILE (Dùng để hiển thị ảnh/video trên trình duyệt)
    // URL: /api/upload/files/{tên_file}
    // ========================================================================
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName, HttpServletRequest request) {
        // 1. Load file từ Service
        Resource resource = storageService.loadFileAsResource(fileName);

        // 2. Xác định Content-Type (Để trình duyệt biết là Ảnh hay Video)
        String contentType = null;
        try {
            // Tự động nhận diện (VD: image/jpeg, video/mp4, application/pdf...)
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Không xác định được loại file.");
        }

        // Nếu không nhận diện được thì để mặc định là binary
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // 3. Trả về file kèm Header chuẩn
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // "inline" nghĩa là hiển thị luôn, "attachment" nghĩa là tải về
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}