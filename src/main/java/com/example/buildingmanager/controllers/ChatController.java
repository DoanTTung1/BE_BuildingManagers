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
    private final GeminiService geminiService; // <--- Inject Service mới

    // Danh sách quận hệ thống hỗ trợ
    private static final String[] KNOWN_DISTRICTS = {
        "Quận 1", "Quận 2", "Quận 3", "Quận 4", "Bình Thạnh", "Phú Nhuận"
    };

    @PostMapping
    public ResponseEntity<String> handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.ok("Chào bạn! Tôi có thể giúp gì cho bạn?");
        }

        // 1. Lấy dữ liệu từ DB
        String dbContext = getDatabaseContext(userMessage);

        // 2. Tạo Prompt
        String prompt = createPrompt(userMessage, dbContext);

        // 3. Gọi Gemini
        String aiResponse = geminiService.callGemini(prompt);

        return ResponseEntity.ok(aiResponse);
    }

    // --- Hàm lấy dữ liệu DB (Giữ nguyên như cũ) ---
    private String getDatabaseContext(String message) {
        String msgLower = removeAccent(message.toLowerCase());
        StringBuilder context = new StringBuilder();

        String foundDistrict = null;
        for (String dist : KNOWN_DISTRICTS) {
            if (msgLower.contains(removeAccent(dist.toLowerCase()))) {
                foundDistrict = dist;
                break;
            }
        }

        if (foundDistrict != null) {
            List<Building> buildings = buildingRepository.findByDistrictName(foundDistrict);
            
            if (buildings.isEmpty()) {
                context.append("Hệ thống: Không tìm thấy tòa nhà nào ở ").append(foundDistrict).append(".\n");
            } else {
                context.append("Dữ liệu tòa nhà thực tế tại ").append(foundDistrict).append(":\n");
                buildings.stream().limit(5).forEach(b -> {
                    context.append("- Tên: ").append(b.getName())
                           .append(" | Giá: ").append(b.getRentPrice()).append(" USD/m2")
                           .append(" | Địa chỉ: ").append(b.getStreet())
                           .append(" | Quản lý: ").append(b.getManagerName() != null ? b.getManagerName() : "Hotline")
                           .append(" (SĐT: ").append(b.getManagerPhoneNumber() != null ? b.getManagerPhoneNumber() : "0912345678")
                           .append(")\n");
                });
            }
        } else {
            context.append("Hệ thống: Chúng tôi có văn phòng tại các quận: 1, 2, 3, 4, Bình Thạnh, Phú Nhuận.\n");
        }
        return context.toString();
    }

    // --- Hàm tạo Prompt (Tinh chỉnh cho ChatGPT) ---
    private String createPrompt(String userQuestion, String dbContext) {
        return """
            Bạn là trợ lý ảo của hệ thống 'Building Manager'.
            Hãy trả lời khách hàng dựa trên dữ liệu dưới đây.
            
            [DỮ LIỆU]:
            %s
            
            [YÊU CẦU]:
            - Ngắn gọn, thân thiện, dùng emoji.
            - Nếu có dữ liệu tòa nhà, hãy đưa ra Tên, Giá và SĐT Quản lý.
            - Nếu không có, gợi ý khách tìm khu vực khác.
            
            [KHÁCH HỎI]: "%s"
            """.formatted(dbContext, userQuestion);
    }

    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ','d').replace('Đ','d');
    }
}