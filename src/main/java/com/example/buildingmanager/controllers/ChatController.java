package com.example.buildingmanager.controllers;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.services.GroqService;
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
    private final GroqService groqService;

    // Map chứa các tên gọi tắt của Quận (Alias)
    private static final Map<String, String> DISTRICT_ALIAS = new HashMap<>();
    static {
        DISTRICT_ALIAS.put("q1", "Quận 1");
        DISTRICT_ALIAS.put("quan 1", "Quận 1");
        DISTRICT_ALIAS.put("q2", "Quận 2");
        DISTRICT_ALIAS.put("quan 2", "Quận 2");
        DISTRICT_ALIAS.put("q3", "Quận 3");
        DISTRICT_ALIAS.put("quan 3", "Quận 3");
        DISTRICT_ALIAS.put("q4", "Quận 4");
        DISTRICT_ALIAS.put("quan 4", "Quận 4");
        DISTRICT_ALIAS.put("binh thanh", "Quận Bình Thạnh");
        DISTRICT_ALIAS.put("bt", "Quận Bình Thạnh");
        DISTRICT_ALIAS.put("phu nhuan", "Quận Phú Nhuận");
        DISTRICT_ALIAS.put("pn", "Quận Phú Nhuận");
        DISTRICT_ALIAS.put("tan binh", "Quận Tân Bình");
        DISTRICT_ALIAS.put("tb", "Quận Tân Bình");
    }

    @PostMapping
    public ResponseEntity<String> handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.ok(
                    "Chào bạn! Mình là Tùng AI - Trợ lý siêu cấp vip pro. Bạn cần tìm nhà hay cần người tâm sự mỏng? 😎");
        }

        // 1. Phân tích & Lấy dữ liệu thông minh
        String dbContext = getSmartDatabaseContext(userMessage);

        // 2. Tạo Prompt với "Nhân cách Đa chiều"
        String prompt = createSuperSmartPrompt(userMessage, dbContext);

        // 3. Gọi Groq (Llama 3)
        String aiResponse = groqService.callGroq(prompt);

        return ResponseEntity.ok(aiResponse);
    }

    // --- LOGIC TÌM KIẾM THÔNG MINH ---
    private String getSmartDatabaseContext(String message) {
        String msgLower = removeAccent(message.toLowerCase());

        // A. Xác định Quận
        String targetDistrict = null;
        for (Map.Entry<String, String> entry : DISTRICT_ALIAS.entrySet()) {
            if (msgLower.contains(entry.getKey())) {
                targetDistrict = entry.getValue();
                break;
            }
        }

        // B. Xác định Ngân sách
        Integer maxPrice = extractNumber(msgLower);

        // C. Truy vấn và Lọc dữ liệu
        List<Building> allBuildings = buildingRepository.findAll();
        String finalTargetDistrict = targetDistrict;

        List<Building> filteredBuildings = allBuildings.stream()
                .filter(b -> finalTargetDistrict == null ||
                        (b.getDistrict() != null && b.getDistrict().getName().equalsIgnoreCase(finalTargetDistrict)))
                .filter(b -> maxPrice == null || b.getRentPrice() <= maxPrice)
                .limit(3) // Lấy 3 cái tốt nhất để AI tập trung tư vấn
                .collect(Collectors.toList());

        // D. Tạo context gửi cho AI
        StringBuilder context = new StringBuilder();

        // Nếu câu hỏi KHÔNG LIÊN QUAN đến tìm nhà (Ví dụ: "Em buồn quá", "Tư vấn tình
        // yêu")
        // Ta vẫn gửi data rỗng để AI tự quyết định cách trả lời.
        if (filteredBuildings.isEmpty()) {
            if (targetDistrict != null) {
                // Khách có ý định tìm nhà nhưng không có dữ liệu
                context.append("Hệ thống: Khu vực ").append(targetDistrict)
                        .append(maxPrice != null ? " giá dưới " + maxPrice + "$" : "")
                        .append(" đang tạm hết. Hãy khéo léo lái khách sang quận khác.\n");
            } else {
                // Khách hỏi chuyện linh tinh hoặc không xác định được ý định
                context.append(
                        "Hệ thống: Không tìm thấy dữ liệu bất động sản liên quan. Hãy trả lời tự do theo ngữ cảnh câu chuyện.\n");
            }
        } else {
            context.append("DANH SÁCH TÒA NHÀ PHÙ HỢP (Dùng để chốt sale):\n");
            filteredBuildings.forEach(b -> {
                context.append("--- 🏢 ").append(b.getName().toUpperCase()).append(" ---\n")
                        .append("- Giá: ").append(b.getRentPrice()).append(" USD/m2\n")
                        .append("- Đ/c: ").append(b.getStreet()).append(", ").append(b.getWard()).append("\n")
                        .append("- Điểm nhấn: ")
                        .append(b.getRentPriceDescription() != null ? b.getRentPriceDescription()
                                : "View đẹp, vị trí đắc địa")
                        .append("\n")
                        .append("- 📞 Quản lý: ").append(b.getManagerName()).append(" (SĐT: ")
                        .append(b.getManagerPhoneNumber()).append(")\n\n");
            });
        }

        return context.toString();
    }

    // --- PROMPT "SIÊU TRÍ TUỆ" & "NHÂN CÁCH NGƯỜI THẬT" ---
    private String createSuperSmartPrompt(String userQuestion, String dbContext) {
        return """
                [SYSTEM INSTRUCTION]
                Bạn là "Tùng AI" - Một nhân viên Sale Bất Động Sản "thực chiến" tại Sài Gòn.
                Tính cách: Thông minh, hài hước, đôi khi hơi "xéo xắc" nhưng rất duyên dáng. Không nói chuyện như cái máy.

                [NHIỆM VỤ ĐA NĂNG]:

                🔹 TRƯỜNG HỢP 1: KHÁCH HỎI MUA/THUÊ NHÀ
                - Dùng dữ liệu dưới đây để tư vấn.
                - Mục tiêu duy nhất: Bắt khách gọi cho SĐT Quản lý.
                - Nếu không có nhà phù hợp: Hãy xin lỗi thật lòng và gợi ý quận khác (VD: "Quận 1 hết rồi, sang Bình Thạnh chơi với em không?").

                🔹 TRƯỜNG HỢP 2: KHÁCH HỎI TÌNH YÊU / ĐỜI SỐNG / TÂM SỰ
                - Đừng từ chối trả lời! Hãy đóng vai "Chuyên gia tư vấn tình cảm".
                - Lời khuyên phải "chất", thực tế và vui vẻ.
                - KỸ THUẬT CAO CẤP: Sau khi tư vấn tình cảm xong, hãy tìm cách "lái" câu chuyện về việc mua nhà một cách hài hước.
                (Ví dụ: "Thất tình thì buồn thật, nhưng buồn trong căn Penhouse Quận 1 vẫn đỡ hơn buồn ngoài công viên đúng không? Ghé xem căn này đi...")

                [DỮ LIỆU HỆ THỐNG CUNG CẤP]:
                %s

                [YÊU CẦU VỀ GIỌNG VĂN]:
                - Tự nhiên, dùng ngôi "mình" - "bạn" hoặc "em" - "anh/chị".
                - Bắt buộc dùng Emoji để cảm xúc hơn (😂, 😭, 😈, 💸, 🏠).
                - Không trả lời quá dài dòng văn tự.

                [USER HỎI]: "%s"

                [TÙNG AI TRẢ LỜI]:
                """
                .formatted(dbContext, userQuestion);
    }

    // --- CÁC HÀM TIỆN ÍCH ---

    // Rút trích số tiền thông minh hơn (Hỗ trợ định dạng 1.000, 10tr, 1000$)
    private Integer extractNumber(String text) {
        text = text.replace(".", "").replace(",", ""); // Xóa dấu chấm phẩy
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(text);

        int maxVal = 0;
        boolean found = false;

        while (m.find()) {
            int val = Integer.parseInt(m.group());
            // Lọc bớt mấy số nhỏ như Quận 1, Quận 3... chỉ lấy số lớn (giá tiền)
            if (val > 10) {
                maxVal = Math.max(maxVal, val);
                found = true;
            }
        }
        return found ? maxVal : null;
    }

    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'd');
    }
}