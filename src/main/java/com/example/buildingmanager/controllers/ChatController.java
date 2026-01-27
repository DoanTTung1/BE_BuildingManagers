package com.example.buildingmanager.controllers;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.services.GroqService;
import lombok.Builder;
import lombok.Data;
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
        DISTRICT_ALIAS.put("q7", "Quận 7");
        DISTRICT_ALIAS.put("quan 7", "Quận 7");
    }

    // 2. DATA QUẬN LÂN CẬN
    private static final Map<String, String> NEIGHBOR_DISTRICTS = new HashMap<>();
    static {
        NEIGHBOR_DISTRICTS.put("Quận 1", "Quận 3, Quận 4 hoặc Bình Thạnh");
        NEIGHBOR_DISTRICTS.put("Quận 3", "Quận 1 hoặc Phú Nhuận");
        NEIGHBOR_DISTRICTS.put("Quận 4", "Quận 1 hoặc Quận 7");
        NEIGHBOR_DISTRICTS.put("Quận Bình Thạnh", "Quận 1 hoặc Phú Nhuận");
    }

    // 3. QUẢN LÝ ẢO
    private static final List<String> RANDOM_MANAGERS = Arrays.asList(
            "Anh Nam (Trưởng phòng KD)", "Chị Linh (Tư vấn viên)", "Anh Hưng (Quản lý khu vực)",
            "Chị Vy (CSKH)", "Anh Tuấn (Sales Manager)");

    // DTO để hứng tiêu chí tìm kiếm
    @Data
    @Builder
    private static class SearchCriteria {
        String district;
        Integer maxPrice;
        Integer minArea;
        Integer minBasement;
        String keywordName; // Tên tòa nhà (nếu khách search đích danh)
    }

    @PostMapping
    public ResponseEntity<String> handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.ok(
                    "Xin chào! Mình là Trợ lý ảo AI. Bạn đang tìm văn phòng khu vực nào, diện tích hay ngân sách khoảng bao nhiêu? 🏢");
        }

        // 1. Phân tích ngữ nghĩa (Extract Intent)
        SearchCriteria criteria = analyzeMessage(userMessage);

        // 2. Lọc dữ liệu DB
        String dbContext = getSmartDatabaseContext(criteria);

        // 3. Tạo Prompt & Gọi AI
        String prompt = createSuperSmartPrompt(userMessage, dbContext);
        String aiResponse = groqService.callGroq(prompt);

        return ResponseEntity.ok(aiResponse);
    }

    // --- PHÂN TÍCH TIN NHẮN (BRAIN 1) ---
    private SearchCriteria analyzeMessage(String message) {
        String msgLower = removeAccent(message.toLowerCase());

        // A. Tìm Quận
        String district = null;
        for (Map.Entry<String, String> entry : DISTRICT_ALIAS.entrySet()) {
            if (msgLower.contains(entry.getKey())) {
                district = entry.getValue();
                break; // Ưu tiên quận đầu tiên tìm thấy
            }
        }

        // B. Tìm Giá (Logic: Số < 100 thường là giá thuê $/m2)
        Integer maxPrice = extractNumberByRegex(msgLower, "gia|usd|do|tine|tien", 100);
        // Nếu không tìm thấy bằng keyword, thử tìm số nhỏ < 60 (giá thị trường)
        if (maxPrice == null)
            maxPrice = extractSimpleNumber(msgLower, 1, 60);

        // C. Tìm Diện tích (Logic: Số > 60 thường là m2)
        Integer minArea = extractNumberByRegex(msgLower, "m2|met|dien tich|rong", 10000);
        if (minArea == null)
            minArea = extractSimpleNumber(msgLower, 61, 5000);

        // D. Tìm số hầm
        Integer minBasement = extractNumberByRegex(msgLower, "ham|cho de xe", 5);

        // E. Tìm tên tòa nhà (Keyword còn lại)
        // Đây là logic đơn giản: Nếu user nhắc tên tòa nhà cụ thể (ví dụ: "Landmark")
        // Thực tế cần ElasticSearch, ở đây ta dùng heuristic đơn giản là tìm trong DB
        // sau.
        String keyword = message; // Tạm thời để nguyên message để lọc contains

        return SearchCriteria.builder()
                .district(district)
                .maxPrice(maxPrice)
                .minArea(minArea)
                .minBasement(minBasement)
                .keywordName(keyword)
                .build();
    }

    // --- TRUY VẤN DB (BRAIN 2) ---
    private String getSmartDatabaseContext(SearchCriteria criteria) {
        List<Building> allBuildings = buildingRepository.findAll();

        // Lọc Stream
        List<Building> filteredBuildings = allBuildings.stream()
                // 1. Lọc Quận
                .filter(b -> criteria.district == null
                        || (b.getDistrict() != null && b.getDistrict().getName().equalsIgnoreCase(criteria.district)))
                // 2. Lọc Giá (Chấp nhận chênh lệch 10%)
                .filter(b -> criteria.maxPrice == null || b.getRentPrice() <= (criteria.maxPrice * 1.1))
                // 3. Lọc Diện tích sàn (Lấy các tòa có sàn >= nhu cầu hoặc chênh lệch chút xíu)
                .filter(b -> criteria.minArea == null
                        || (b.getFloorArea() != null && b.getFloorArea() >= (criteria.minArea * 0.8)))
                // 4. Lọc Số hầm
                .filter(b -> criteria.minBasement == null || getBasementCount(b) >= criteria.minBasement)
                // 5. Lọc Tên (Nếu user gõ đúng tên tòa nhà trong message)
                .sorted((b1, b2) -> {
                    // Ưu tiên tòa nhà nào có tên xuất hiện trong message của user
                    boolean b1Match = removeAccent(criteria.keywordName.toLowerCase())
                            .contains(removeAccent(b1.getName().toLowerCase()));
                    boolean b2Match = removeAccent(criteria.keywordName.toLowerCase())
                            .contains(removeAccent(b2.getName().toLowerCase()));
                    return Boolean.compare(b2Match, b1Match); // True lên đầu
                })
                .limit(4) // Lấy tối đa 4 kết quả
                .collect(Collectors.toList());

        // Tạo Context gửi AI
        StringBuilder context = new StringBuilder();

        // Debug info để AI hiểu mình đang lọc theo cái gì
        context.append("Hệ thống đã lọc theo: ")
                .append(criteria.district != null ? "Quận: " + criteria.district + ", " : "")
                .append(criteria.maxPrice != null ? "Giá < " + criteria.maxPrice + "$, " : "")
                .append(criteria.minArea != null ? "Diện tích > " + criteria.minArea + "m2, " : "")
                .append(criteria.minBasement != null ? "Hầm > " + criteria.minBasement : "")
                .append("\n\n");

        if (filteredBuildings.isEmpty()) {
            if (criteria.district != null) {
                String neighbors = NEIGHBOR_DISTRICTS.getOrDefault(criteria.district, "các quận lân cận");
                context.append("Hệ thống: Không tìm thấy tòa nhà nào khớp 100% tiêu chí tại ").append(criteria.district)
                        .append(". HÃY GỢI Ý KHÁCH TÌM SANG: ").append(neighbors)
                        .append(" hoặc điều chỉnh ngân sách.\n");
            } else {
                context.append("Hệ thống: Không tìm thấy dữ liệu. Hãy hỏi thêm chi tiết để tư vấn lại.\n");
            }
        } else {
            context.append("DANH SÁCH TÒA NHÀ PHÙ HỢP (Dữ liệu thực tế):\n");
            filteredBuildings.forEach(b -> {
                String manager = (b.getManagerName() != null && !b.getManagerName().isEmpty()) ? b.getManagerName()
                        : getRandomManager();
                String phone = (b.getManagerPhoneNumber() != null && !b.getManagerPhoneNumber().isEmpty())
                        ? b.getManagerPhoneNumber()
                        : "0909" + new Random().nextInt(999999);
                int hames = getBasementCount(b); // Lấy số hầm giả lập từ description nếu null

                context.append("--- 🏢 ").append(b.getName().toUpperCase()).append(" ---\n")
                        .append("- Giá thuê: ").append(b.getRentPrice()).append(" USD/m2\n")
                        .append("- Diện tích sàn: ").append(b.getFloorArea()).append(" m2\n")
                        .append("- Kết cấu: ").append(hames > 0 ? hames + " hầm" : "Có chỗ để xe").append("\n")
                        .append("- Vị trí: ").append(b.getStreet()).append(", ").append(b.getDistrict().getName())
                        .append("\n")
                        .append("- Loại: ").append(b.getType() != null ? b.getType() : "Văn phòng chuẩn").append("\n")
                        .append("- 📞 LIÊN HỆ: ").append(manager).append(" (").append(phone).append(")\n\n");
            });
        }
        return context.toString();
    }

    // --- PROMPT "NHÂN CÁCH HÓA" (SOUL) ---
    private String createSuperSmartPrompt(String userQuestion, String dbContext) {
        return """
                [VAI TRÒ]
                Bạn là chuyên gia tư vấn Bất Động Sản cao cấp tên là "EliteBot".
                Phong cách: Chuyên nghiệp, am hiểu thị trường, dùng từ ngữ sang trọng nhưng thân thiện.

                [NHIỆM VỤ]:
                1. Trả lời câu hỏi của khách hàng dựa trên [DỮ LIỆU HỆ THỐNG].
                2. Nếu có tòa nhà phù hợp: Hãy giới thiệu 1-2 tòa nhà tốt nhất. Nhấn mạnh vào Ưu điểm (Giá tốt, Vị trí đẹp, Diện tích rộng).
                3. Luôn kêu gọi hành động (Call To Action): Mời khách để lại SĐT hoặc gọi cho Quản lý.
                4. Nếu khách hỏi đời tư/xã giao: Hãy trả lời vui vẻ và khéo léo lái về việc thuê văn phòng.

                [DỮ LIỆU HỆ THỐNG]:
                %s

                [USER HỎI]: "%s"

                [EliteBot TRẢ LỜI]:
                """
                .formatted(dbContext, userQuestion);
    }

    // --- CÁC HÀM XỬ LÝ SỐ LIỆU & REGEX ---

    // Tìm số đứng gần các từ khóa (Ví dụ: "giá 20", "20 usd", "diện tích 100")
    private Integer extractNumberByRegex(String text, String keywords, int limit) {
        // Regex tìm số đứng trước hoặc sau từ khóa
        // Ví dụ pattern: (\d+)\s*(usd|do)|(gia)\s*(\d+)
        try {
            Pattern p = Pattern.compile("(\\d+)\\s*(" + keywords + ")|(" + keywords + ")\\s*(\\d+)");
            Matcher m = p.matcher(text);
            if (m.find()) {
                // Group 1 là số đứng trước, Group 4 là số đứng sau
                String numStr = m.group(1) != null ? m.group(1) : m.group(4);
                return Integer.parseInt(numStr);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    // Tìm số đơn thuần nằm trong khoảng min-max (Heuristic)
    private Integer extractSimpleNumber(String text, int min, int max) {
        text = text.replace(".", "").replace(",", "");
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(text);
        while (m.find()) {
            try {
                int val = Integer.parseInt(m.group());
                if (val >= min && val <= max)
                    return val;
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    // Helper: Lấy số hầm an toàn (vì DB có thể null hoặc lưu dạng String/Code)
    private int getBasementCount(Building b) {
        // Giả sử DB chưa có cột basement cụ thể, ta check trong description hoặc
        // structure
        // Ở đây mình giả lập logic, bạn thay bằng b.getNumberOfBasement() nếu có
        String desc = (b.getStructure() + " " + b.getRentPriceDescription()).toLowerCase();
        if (desc.contains("2 hầm") || desc.contains("2 tang ham"))
            return 2;
        if (desc.contains("3 hầm") || desc.contains("3 tang ham"))
            return 3;
        if (desc.contains("hầm") || desc.contains("ham"))
            return 1;
        return 0; // Mặc định
    }

    private String getRandomManager() {
        return RANDOM_MANAGERS.get(new Random().nextInt(RANDOM_MANAGERS.size()));
    }

    public static String removeAccent(String s) {
        if (s == null)
            return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'd');
    }
}