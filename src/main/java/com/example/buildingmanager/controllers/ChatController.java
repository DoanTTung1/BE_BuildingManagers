package com.example.buildingmanager.controllers;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.repositories.BuildingRepository;
import com.example.buildingmanager.services.GroqService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    private final BuildingRepository buildingRepository;
    private final GroqService groqService;

    // --- 1. CONFIG DATA ---
    private static final Map<String, String> DISTRICT_ALIAS = new HashMap<>();
    static {
        DISTRICT_ALIAS.put("q1", "Quận 1");
        DISTRICT_ALIAS.put("q2", "Quận 2");
        DISTRICT_ALIAS.put("q3", "Quận 3");
        DISTRICT_ALIAS.put("q4", "Quận 4");
        DISTRICT_ALIAS.put("binh thanh", "Bình Thạnh");
        DISTRICT_ALIAS.put("phu nhuan", "Phú Nhuận");
        DISTRICT_ALIAS.put("tan binh", "Tân Bình");
        DISTRICT_ALIAS.put("q7", "Quận 7");
    }

    private static final List<String> MANAGERS = Arrays.asList("Anh Nam", "Chị Linh", "Anh Hưng", "Chị Vy");

    @Data
    @Builder
    private static class SearchCriteria {
        String district;
        Integer maxPrice;
        Integer minArea;
        String keywordName;
        boolean hasBasement;
    }

    @PostMapping
    public ResponseEntity<String> handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.ok("Chào bạn! Bạn cần tìm văn phòng khu vực nào, ngân sách bao nhiêu? 🏢");
        }

        // 1. Phân tích ý định
        SearchCriteria criteria = analyzeMessage(userMessage);

        // 2. Query Database (Lấy dữ liệu thô)
        String dbContext = getDatabaseContextUsingSpec(criteria);

        // 3. Gọi AI (Với Prompt ép kiểu ngắn gọn)
        String prompt = createConcisePrompt(userMessage, dbContext);

        return ResponseEntity.ok(groqService.callGroq(prompt));
    }

    // --- 2. TẠO PROMPT "QUÂN ĐỘI" (NGẮN GỌN - RÕ RÀNG) ---
    private String createConcisePrompt(String userMsg, String dbContext) {
        return """
                [VAI TRÒ] Bạn là Trợ lý BĐS chuyên nghiệp.
                [YÊU CẦU TRẢ LỜI]
                1. TRẢ LỜI NGẮN GỌN, TRỰC DIỆN. Không chào hỏi rườm rà.
                2. Nếu có danh sách tòa nhà: Chỉ liệt kê theo định dạng gạch đầu dòng (-).
                3. Cấu trúc mỗi dòng: Tên Tòa Nhà | Vị trí | Giá | Diện tích | SĐT Liên hệ.
                4. Nếu KHÔNG có dữ liệu: Báo ngắn gọn "Hiện chưa có tòa nhà phù hợp" và gợi ý tìm quận khác.

                [DỮ LIỆU TÌM ĐƯỢC TỪ HỆ THỐNG]:
                %s

                [KHÁCH HỎI]: "%s"
                """.formatted(dbContext, userMsg);
    }

    // --- 3. LẤY DỮ LIỆU SẠCH TỪ DB ---
    private String getDatabaseContextUsingSpec(SearchCriteria criteria) {
        Specification<Building> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.district != null) {
                try {
                    Join<Object, Object> districtJoin = root.join("district");
                    predicates.add(cb.like(districtJoin.get("name"), "%" + criteria.district + "%"));
                } catch (Exception e) {
                }
            }
            if (criteria.maxPrice != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("rentPrice"), criteria.maxPrice * 1.1));
            if (criteria.minArea != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("floorArea"), criteria.minArea));
            if (criteria.keywordName != null)
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.keywordName.toLowerCase() + "%"));
            if (criteria.hasBasement)
                predicates.add(cb.like(cb.lower(root.get("structure")), "%hầm%"));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Lấy 3 kết quả tốt nhất để tránh dài dòng
        Page<Building> resultPage = buildingRepository.findAll(spec,
                PageRequest.of(0, 3, Sort.by("rentPrice").ascending()));
        List<Building> buildings = resultPage.getContent();

        StringBuilder sb = new StringBuilder();
        if (buildings.isEmpty()) {
            sb.append("KHÔNG TÌM THẤY KẾT QUẢ.\n");
        } else {
            for (Building b : buildings) {
                String manager = StringUtils.hasText(b.getManagerName()) ? b.getManagerName()
                        : MANAGERS.get(new Random().nextInt(MANAGERS.size()));
                String phone = StringUtils.hasText(b.getManagerPhoneNumber()) ? b.getManagerPhoneNumber()
                        : "0909" + new Random().nextInt(999999);
                String districtName = b.getDistrict() != null ? b.getDistrict().getName() : "";

                // Format dữ liệu dạng dòng kẻ để AI dễ đọc
                sb.append(String.format("- %s | %s | %s$/m2 | %sm2 | LH: %s (%s)\n",
                        b.getName(), districtName, b.getRentPrice(), b.getFloorArea(), manager, phone));
            }
        }
        return sb.toString();
    }

    // --- 4. LOGIC PHÂN TÍCH (GIỮ NGUYÊN VÌ ĐÃ TỐT) ---
    private SearchCriteria analyzeMessage(String msg) {
        String msgLower = removeAccent(msg.toLowerCase());

        String district = null;
        for (Map.Entry<String, String> entry : DISTRICT_ALIAS.entrySet()) {
            if (msgLower.contains(entry.getKey())) {
                district = entry.getValue();
                break;
            }
        }

        Integer maxPrice = extractNumber(msgLower, "(giá|tầm|khoảng|dưới)\\s*(\\d+)|(\\d+)\\s*(usd|do|$)");
        if (maxPrice == null)
            maxPrice = extractSimpleNumber(msgLower, 5, 80);

        Integer minArea = extractNumber(msgLower, "(\\d+)\\s*(m2|met|vuong)|(dt|dien tich)\\s*(\\d+)");
        if (minArea == null)
            minArea = extractSimpleNumber(msgLower, 60, 5000);

        String keyword = null;
        if (district == null && maxPrice == null && minArea == null)
            keyword = msg.trim();

        boolean hasBasement = msgLower.contains("ham") || msgLower.contains("oto");

        return SearchCriteria.builder()
                .district(district)
                .maxPrice(maxPrice)
                .minArea(minArea)
                .keywordName(keyword)
                .hasBasement(hasBasement)
                .build();
    }

    private Integer extractNumber(String text, String regex) {
        try {
            Matcher m = Pattern.compile(regex).matcher(text);
            if (m.find()) {
                for (int i = 1; i <= m.groupCount(); i++) {
                    if (m.group(i) != null && m.group(i).matches("\\d+"))
                        return Integer.parseInt(m.group(i));
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private Integer extractSimpleNumber(String text, int min, int max) {
        Matcher m = Pattern.compile("\\d+").matcher(text.replace(".", ""));
        while (m.find()) {
            int val = Integer.parseInt(m.group());
            if (val >= min && val <= max)
                return val;
        }
        return null;
    }

    public static String removeAccent(String s) {
        if (s == null)
            return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(temp).replaceAll("").replace('đ', 'd')
                .replace('Đ', 'd');
    }
}