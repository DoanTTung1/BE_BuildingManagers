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

    // 1. DATA QUẬN & VIẾT TẮT
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

    // 2. DATA QUẬN LÂN CẬN (Để gợi ý khi hết hàng)
    private static final Map<String, String> NEIGHBOR_DISTRICTS = new HashMap<>();
    static {
        NEIGHBOR_DISTRICTS.put("Quận 1", "Quận 3, Quận 4 hoặc Bình Thạnh");
        NEIGHBOR_DISTRICTS.put("Quận 3", "Quận 1 hoặc Phú Nhuận");
        NEIGHBOR_DISTRICTS.put("Quận 4", "Quận 1 hoặc Quận 7");
        NEIGHBOR_DISTRICTS.put("Quận Bình Thạnh", "Quận 1 hoặc Phú Nhuận");
    }

    // 3. DANH SÁCH QUẢN LÝ ẢO (Tạo cảm giác chuyên nghiệp)
    private static final List<String> RANDOM_MANAGERS = Arrays.asList(
            "Anh Nam (Trưởng phòng KD)", "Chị Linh (Tư vấn viên)", "Anh Hưng (Quản lý khu vực)",
            "Chị Vy (Chăm sóc khách hàng)", "Anh Tuấn (Sales Manager)", "Chị Thảo (Admin)");

    @PostMapping
    public ResponseEntity<String> handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");

        // Lời chào chuyên nghiệp
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.ok(
                    "Xin chào! Mình là Trợ lý ảo AI. Mình có thể giúp bạn tìm văn phòng theo ngân sách hoặc khu vực nào? 🏢");
        }

        // 1. Phân tích & Lấy dữ liệu thông minh
        String dbContext = getSmartDatabaseContext(userMessage);

        // 2. Tạo Prompt (Kịch bản)
        String prompt = createSuperSmartPrompt(userMessage, dbContext);

        // 3. Gọi AI
        String aiResponse = groqService.callGroq(prompt);

        return ResponseEntity.ok(aiResponse);
    }

    // --- LOGIC TÌM KIẾM THÔNG MINH (BRAIN) ---
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

        // C. Truy vấn và Lọc
        List<Building> allBuildings = buildingRepository.findAll();
        String finalTargetDistrict = targetDistrict;

        List<Building> filteredBuildings = allBuildings.stream()
                .filter(b -> finalTargetDistrict == null ||
                        (b.getDistrict() != null && b.getDistrict().getName().equalsIgnoreCase(finalTargetDistrict)))
                // 🔥 THÔNG MINH: Cho phép chênh lệch giá 10% (Ví dụ khách tìm 1000, hiển thị cả
                // 1100)
                .filter(b -> maxPrice == null || b.getRentPrice() <= (maxPrice * 1.1))
                .limit(3)
                .collect(Collectors.toList());

        // D. Tạo Context gửi AI
        StringBuilder context = new StringBuilder();

        if (filteredBuildings.isEmpty()) {
            if (targetDistrict != null) {
                // 🔥 THÔNG MINH: Gợi ý quận lân cận
                String neighbors = NEIGHBOR_DISTRICTS.getOrDefault(targetDistrict, "các quận trung tâm khác");
                context.append("Hệ thống: Hiện tại ").append(targetDistrict)
                        .append(maxPrice != null ? " mức giá " + maxPrice + "$" : "")
                        .append(" đã hết phòng. HÃY GỢI Ý KHÁCH SANG: ").append(neighbors).append(".\n");
            } else {
                context.append(
                        "Hệ thống: Không tìm thấy dữ liệu BĐS phù hợp. Hãy trả lời xã giao vui vẻ, lái câu chuyện về Bất động sản.\n");
            }
        } else {
            context.append("DANH SÁCH TÒA NHÀ PHÙ HỢP (Ưu tiên chốt đơn các căn này):\n");
            filteredBuildings.forEach(b -> {
                // Xử lý tên quản lý ảo
                String managerName = b.getManagerName();
                if (managerName == null || managerName.trim().isEmpty())
                    managerName = getRandomManager();

                String phone = b.getManagerPhoneNumber();
                if (phone == null || phone.trim().isEmpty())
                    phone = "09" + (10000000 + new Random().nextInt(90000000));

                context.append("--- 🏢 ").append(b.getName().toUpperCase()).append(" ---\n")
                        .append("- Giá thuê: ").append(b.getRentPrice()).append(" USD/m2\n")
                        .append("- Vị trí: ").append(b.getStreet()).append(", ").append(b.getWard()).append("\n")
                        .append("- Đặc điểm: ")
                        .append(b.getRentPriceDescription() != null ? b.getRentPriceDescription()
                                : "Văn phòng hạng A, View đẹp")
                        .append("\n")
                        .append("- 📞 LIÊN HỆ NGAY: ").append(managerName).append(" - SĐT: ").append(phone)
                        .append("\n\n");
            });
        }

        return context.toString();
    }

    // --- PROMPT "NHÂN CÁCH HÓA" (SOUL) ---
    private String createSuperSmartPrompt(String userQuestion, String dbContext) {
        return """
                [VAI TRÒ]
                Bạn là "Trợ lý ảo Bất Động Sản" cao cấp.
                Phong cách: Chuyên nghiệp, Tinh tế, Nhiệt tình nhưng không chèo kéo.

                [NHIỆM VỤ]:
                1. TƯ VẤN BĐS:
                   - Dựa vào dữ liệu được cung cấp.
                   - Nếu tìm thấy nhà: Hãy mô tả hấp dẫn (dùng từ "siêu phẩm", "cực hot", "view triệu đô"). Bắt buộc cung cấp SĐT Quản lý.
                   - Nếu KHÔNG thấy nhà: Đừng nói "Không có". Hãy nói "Hiện tại khu vực này đang cháy hàng, nhưng bên mình còn mấy căn cực đẹp ở [GỢI Ý TỪ HỆ THỐNG]...".

                2. TƯ VẤN ĐỜI SỐNG (Khi khách than vãn/tâm sự):
                   - Hãy lắng nghe và chia sẻ như một người bạn tri kỷ.
                   - Tuyệt chiêu "LÁI SALE": Sau khi an ủi, hãy khéo léo gắn câu chuyện của họ vào lợi ích của việc có một văn phòng/ngôi nhà mới.

                [DỮ LIỆU HỆ THỐNG]:
                %s

                [YÊU CẦU ĐỊNH DẠNG]:
                - Dùng Emoji tinh tế (✨, 🏢, 🤝, 💎).
                - Xưng hô: "Mình" - "Bạn" (Thân thiện).
                - Câu trả lời ngắn gọn, tạo cảm giác tò mò để khách hỏi tiếp.

                [USER HỎI]: "%s"

                [TRỢ LÝ ẢO TRẢ LỜI]:
                """
                .formatted(dbContext, userQuestion);
    }

    // --- CÁC HÀM BỔ TRỢ ---
    private String getRandomManager() {
        return RANDOM_MANAGERS.get(new Random().nextInt(RANDOM_MANAGERS.size()));
    }

    private Integer extractNumber(String text) {
        text = text.replace(".", "").replace(",", "");
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(text);
        int maxVal = 0;
        boolean found = false;
        while (m.find()) {
            int val = Integer.parseInt(m.group());
            // Logic thông minh: Bỏ qua các số nhỏ (như tên Quận 1, Quận 3) chỉ lấy giá tiền
            // (>50)
            if (val > 50) {
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