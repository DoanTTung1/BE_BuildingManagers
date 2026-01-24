package com.example.buildingmanager.controllers;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.services.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    private final BuildingRepository buildingRepository;
    private final GeminiService geminiService;

    // Danh sách quận hệ thống hỗ trợ (để lọc dữ liệu sơ bộ)
    private static final String[] KNOWN_DISTRICTS = {
            "Quận 1", "Quận 2", "Quận 3", "Quận 4", "Bình Thạnh", "Phú Nhuận"
    };

    @PostMapping
    public ResponseEntity<String> handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.ok("Chào bạn! Tôi có thể giúp gì cho bạn?");
        }

        // Bước 1: Lấy dữ liệu thô từ Database (Retrieval)
        String dbContext = getDatabaseContext(userMessage);

        // Bước 2: Tạo đề bài (Prompt) cho Gemini
        String prompt = createPrompt(userMessage, dbContext);

        // Bước 3: Gọi Gemini để sinh câu trả lời (Generation)
        String aiResponse = geminiService.callGemini(prompt);

        return ResponseEntity.ok(aiResponse);
    }

    // --- Hàm tìm dữ liệu từ SQL để "mớm" cho AI ---
    private String getDatabaseContext(String message) {
        String msgLower = removeAccent(message.toLowerCase());
        StringBuilder context = new StringBuilder();

        // 1. Kiểm tra xem khách có nhắc đến Quận nào không
        String foundDistrict = null;
        for (String dist : KNOWN_DISTRICTS) {
            if (msgLower.contains(removeAccent(dist.toLowerCase()))) {
                foundDistrict = dist;
                break;
            }
        }

        // 2. Nếu có quận -> Query DB lấy thông tin tòa nhà
        if (foundDistrict != null) {
            // Gọi hàm tìm kiếm theo tên quận trong Repository
            List<Building> buildings = buildingRepository.findByDistrictName(foundDistrict);

            if (buildings.isEmpty()) {
                context.append("Hệ thống: Hiện tại không còn tòa nhà trống nào tại ").append(foundDistrict)
                        .append(".\n");
            } else {
                context.append("Dữ liệu tòa nhà thực tế tại ").append(foundDistrict).append(":\n");
                // Chỉ lấy 5 tòa tiêu biểu để gửi cho AI (tiết kiệm token)
                buildings.stream().limit(5).forEach(b -> {
                    context.append("- Tên: ").append(b.getName())
                            .append(" | Giá thuê: ").append(b.getRentPrice()).append(" USD/m2")
                            .append(" | Diện tích: ").append(b.getFloorArea()).append("m2")
                            .append(" | Địa chỉ: ").append(b.getStreet())
                            .append("\n");
                });
            }
        } else {
            // Nếu không hỏi quận cụ thể
            context.append("Hệ thống: Chúng tôi hiện có văn phòng tại các quận: 1, 2, 3, 4, Bình Thạnh, Phú Nhuận.\n");
            context.append("Hotline hỗ trợ: 0912.345.678 (Mr. Tùng).\n");
        }

        return context.toString();
    }

    // --- Hàm tạo Prompt (Kịch bản cho AI) ---
    private String createPrompt(String userQuestion, String dbContext) {
        return """
                Bạn là trợ lý ảo bán hàng chuyên nghiệp của hệ thống 'Building Manager'.
                Hãy trả lời câu hỏi của khách hàng dựa trên dữ liệu hệ thống cung cấp dưới đây.

                [DỮ LIỆU HỆ THỐNG]:
                %s

                [YÊU CẦU]:
                1. Trả lời ngắn gọn, thân thiện, dùng icon emoji vui vẻ.
                2. Nếu có dữ liệu tòa nhà, hãy liệt kê tên và giá để khách tham khảo.
                3. Nếu không có dữ liệu, hãy khéo léo gợi ý khách tìm khu vực khác hoặc gọi Hotline.
                4. Tuyệt đối KHÔNG bịa đặt thông tin tòa nhà không có trong dữ liệu.

                [KHÁCH HỎI]: "%s"
                """.formatted(dbContext, userQuestion);
    }

    // Tiện ích: Xóa dấu tiếng Việt
    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'd');
    }
}