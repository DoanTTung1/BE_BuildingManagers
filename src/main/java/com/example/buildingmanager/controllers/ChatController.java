package com.example.buildingmanager.controllers;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.services.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    private final BuildingRepository buildingRepository;
    private final GeminiService geminiService;

    // Map chứa các tên gọi tắt của Quận (Alias)
    private static final Map<String, String> DISTRICT_ALIAS = new HashMap<>();
    static {
        DISTRICT_ALIAS.put("q1", "Quận 1"); DISTRICT_ALIAS.put("quan 1", "Quận 1");
        DISTRICT_ALIAS.put("q2", "Quận 2"); DISTRICT_ALIAS.put("quan 2", "Quận 2");
        DISTRICT_ALIAS.put("q3", "Quận 3"); DISTRICT_ALIAS.put("quan 3", "Quận 3");
        DISTRICT_ALIAS.put("q4", "Quận 4"); DISTRICT_ALIAS.put("quan 4", "Quận 4");
        DISTRICT_ALIAS.put("binh thanh", "Quận Bình Thạnh"); DISTRICT_ALIAS.put("bt", "Quận Bình Thạnh");
        DISTRICT_ALIAS.put("phu nhuan", "Quận Phú Nhuận"); DISTRICT_ALIAS.put("pn", "Quận Phú Nhuận");
    }

    @PostMapping
    public ResponseEntity<String> handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.ok("Chào bạn! Mình là AI hỗ trợ tìm văn phòng. Bạn đang tìm khu vực nào và ngân sách khoảng bao nhiêu?");
        }

        // 1. Phân tích & Lấy dữ liệu thông minh (Smart Retrieval)
        String dbContext = getSmartDatabaseContext(userMessage);

        // 2. Tạo Prompt với nhân cách "Best Seller"
        String prompt = createPersonaPrompt(userMessage, dbContext);

        // 3. Gọi Gemini
        String aiResponse = geminiService.callGemini(prompt);

        return ResponseEntity.ok(aiResponse);
    }

    // --- LOGIC TÌM KIẾM THÔNG MINH (Bộ não) ---
    private String getSmartDatabaseContext(String message) {
        String msgLower = removeAccent(message.toLowerCase());
        
        // A. Xác định Quận (Hiểu cả từ viết tắt: q1, bt, pn...)
        String targetDistrict = null;
        for (Map.Entry<String, String> entry : DISTRICT_ALIAS.entrySet()) {
            if (msgLower.contains(entry.getKey())) {
                targetDistrict = entry.getValue();
                break;
            }
        }

        // B. Xác định Ngân sách (Nếu khách nói "dưới 2000" -> Lọc giá)
        Integer maxPrice = extractNumber(msgLower, "gia", "tien", "usd", "do");

        // C. Truy vấn và Lọc dữ liệu
        // Lưu ý: Tốt nhất là lọc DB, nhưng để demo nhanh ta lấy list về lọc Java Stream
        List<Building> allBuildings = buildingRepository.findAll(); 
        String finalTargetDistrict = targetDistrict;

        List<Building> filteredBuildings = allBuildings.stream()
            // Lọc theo Quận
            .filter(b -> finalTargetDistrict == null || 
                        (b.getDistrict() != null && b.getDistrict().getName().equalsIgnoreCase(finalTargetDistrict)))
            // Lọc theo Giá (Nếu khách có nói giá)
            .filter(b -> maxPrice == null || b.getRentPrice() <= maxPrice)
            .limit(5) // Lấy tối đa 5 kết quả tốt nhất
            .collect(Collectors.toList());

        // D. Tạo context gửi cho AI
        StringBuilder context = new StringBuilder();
        if (filteredBuildings.isEmpty()) {
            if (targetDistrict != null) {
                context.append("Hệ thống: Hiện tại khu vực ").append(targetDistrict)
                       .append(maxPrice != null ? " với mức giá dưới " + maxPrice + " USD" : "")
                       .append(" đang tạm hết phòng. Hãy gợi ý khách xem các quận lân cận.\n");
            } else {
                context.append("Hệ thống: Không tìm thấy tòa nhà phù hợp tiêu chí. Hãy hỏi khách thêm chi tiết về khu vực mong muốn.\n");
            }
        } else {
            context.append("Dữ liệu thực tế tìm được (Ưu tiên tư vấn các tòa này):\n");
            filteredBuildings.forEach(b -> {
                context.append("--- 🏢 TÒA NHÀ ").append(b.getName().toUpperCase()).append(" ---\n")
                       .append("- Giá thuê: ").append(b.getRentPrice()).append(" USD/m2\n")
                       .append("- Diện tích sàn: ").append(b.getFloorArea()).append("m2\n")
                       .append("- Địa chỉ: ").append(b.getStreet()).append(", ").append(b.getWard()).append("\n")
                       .append("- Mô tả: ").append(b.getRentPriceDescription() != null ? b.getRentPriceDescription() : "Văn phòng hạng A, view đẹp, tiện nghi.") .append("\n")
                       .append("- 📞 Liên hệ quản lý: ").append(b.getManagerName()).append(" - SĐT: ").append(b.getManagerPhoneNumber()).append("\n\n");
            });
        }
        
        return context.toString();
    }

    // --- PROMPT KỸ THUẬT CAO (Phần Hồn) ---
    private String createPersonaPrompt(String userQuestion, String dbContext) {
        return """
            [VAI TRÒ CỦA BẠN]
            Bạn là "Trợ Lý Ảo Tùng House" - Chuyên gia tư vấn Bất động sản số 1 TP.HCM.
            Phong cách: Chuyên nghiệp, Nhiệt tình, Nhanh nhẹn và hơi Hài hước một chút.
            Mục tiêu: Giúp khách hàng tìm được văn phòng ưng ý và ĐIỀU HƯỚNG KHÁCH GỌI ĐIỆN CHO QUẢN LÝ.

            [DỮ LIỆU HỆ THỐNG (SỰ THẬT)]:
            %s

            [QUY TẮC TRẢ LỜI - BẮT BUỘC]:
            1. **KHÔNG BỊA ĐẶT**: Chỉ tư vấn dựa trên dữ liệu hệ thống cung cấp. Nếu không có, hãy thành thật xin lỗi và gợi ý giải pháp khác.
            2. **ĐỊNH DẠNG ĐẸP**: Sử dụng các icon emoji (🏢, 💰, 📍, 📞) để bài tư vấn sinh động.
            3. **KỸ NĂNG SALE**:
               - Đừng chỉ liệt kê. Hãy dùng từ ngữ hấp dẫn (Ví dụ: "Căn này siêu hot", "Giá cực mềm").
               - Luôn nhắc đến SĐT quản lý và giục khách gọi ngay kẻo hết.
            4. **NGÔN NGỮ**: Tiếng Việt tự nhiên, thân thiện (dùng từ "mình", "bạn", "ạ", "nhé").

            [KHÁCH HỎI]: "%s"
            
            [CÂU TRẢ LỜI CỦA BẠN (Ngắn gọn dưới 150 từ)]:
            """.formatted(dbContext, userQuestion);
    }

    // --- CÁC HÀM TIỆN ÍCH BỔ TRỢ ---

    // 1. Hàm rút trích số từ câu nói (VD: "dưới 2000" -> lấy số 2000)
    private Integer extractNumber(String text, String... keywords) {
        // Regex tìm số nguyên trong chuỗi
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(text);
        if (m.find()) {
            return Integer.parseInt(m.group());
        }
        return null;
    }

    // 2. Hàm xóa dấu Tiếng Việt
    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ','d').replace('Đ','d');
    }
}