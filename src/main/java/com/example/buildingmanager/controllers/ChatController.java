package com.example.buildingmanager.controllers;

import com.example.buildingmanager.entities.Building;
import com.example.buildingmanager.repositories.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    private final BuildingRepository buildingRepository;

    // Danh sách từ khóa quận để bắt lỗi chính tả hoặc tìm nhanh
    private static final String[] KNOWN_DISTRICTS = {
            "Quận 1", "Quận 2", "Quận 3", "Quận 4",
            "Bình Thạnh", "Phú Nhuận", "Tân Bình"
    };

    @PostMapping
    public ResponseEntity<String> handleChat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.ok("Chào bạn, bạn cần hỗ trợ gì không ạ?");
        }

        String botResponse = processMessage(userMessage);
        return ResponseEntity.ok(botResponse);
    }

    private String processMessage(String message) {
        String msgLower = message.toLowerCase();

        // 1. Chào hỏi
        if (msgLower.contains("xin chào") || msgLower.contains("hello") || msgLower.contains("hi ")) {
            return "Chào bạn! Tôi là AI hỗ trợ tìm kiếm văn phòng. Bạn muốn tìm văn phòng ở Quận mấy (VD: Quận 1, Bình Thạnh)?";
        }

        // 2. Hỏi về Quận (Sử dụng hàm tối ưu từ Repository)
        if (msgLower.contains("quận") || msgLower.contains("khu vực") || msgLower.contains("ở đâu")) {

            String detectedDistrict = null;

            // Quét xem trong tin nhắn có tên quận nào quen thuộc không
            for (String district : KNOWN_DISTRICTS) {
                if (msgLower.contains(district.toLowerCase())) {
                    detectedDistrict = district;
                    break;
                }
            }

            if (detectedDistrict != null) {
                // [QUAN TRỌNG] Gọi đúng tên hàm trong Repository của bạn: findByDistrictName
                List<Building> matches = buildingRepository.findByDistrictName(detectedDistrict);

                if (!matches.isEmpty()) {
                    StringBuilder response = new StringBuilder(
                            "Dạ, tôi tìm thấy " + matches.size() + " tòa nhà ở " + detectedDistrict + " phù hợp:\n");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

                    // Chỉ hiển thị tối đa 3 kết quả
                    matches.stream().limit(3).forEach(b -> {
                        String price = fmt.format(b.getRentPrice()).replace(".00", "");
                        response.append("🏢 ").append(b.getName()).append("\n")
                                .append("   - Giá: ").append(price).append("/m²\n");
                    });

                    response.append("\nBạn bấm vào trang 'Tìm kiếm' để xem chi tiết nhé!");
                    return response.toString();
                } else {
                    return "Hiện tại tôi chưa thấy tòa nhà nào còn trống ở " + detectedDistrict
                            + ". Bạn thử tìm khu vực khác xem sao?";
                }
            } else {
                return "Hệ thống hiện có văn phòng tại: Quận 1, Quận 2, Quận 3, Quận 4, Bình Thạnh, Phú Nhuận. Bạn muốn tìm ở đâu?";
            }
        }

        // 3. Hỏi về Giá
        if (msgLower.contains("giá") || msgLower.contains("tiền") || msgLower.contains("chi phí")) {
            return "Giá thuê bên mình dao động từ $10 - $50/m2 tùy vị trí và hạng tòa nhà. Bạn có thể dùng bộ lọc 'Mức giá' ở trang Tìm kiếm để lọc chính xác ngân sách của mình.";
        }

        // 4. Hỏi về Liên hệ
        if (msgLower.contains("liên hệ") || msgLower.contains("sđt") || msgLower.contains("tư vấn")
                || msgLower.contains("gọi")) {
            return "Bạn có thể liên hệ trực tiếp qua Hotline: 0912.345.678 (Mr. Tùng) hoặc để lại số điện thoại, nhân viên bên mình sẽ gọi lại ngay.";
        }

        // 5. Hỏi Admin
        if (msgLower.contains("admin") || msgLower.contains("quản trị") || msgLower.contains("tác giả")) {
            return "Hệ thống được phát triển bởi nhóm Đoàn Thanh Tùng. Rất vui được hỗ trợ bạn!";
        }

        // Mặc định
        return "Xin lỗi, tôi chưa hiểu rõ ý bạn lắm. Bạn có thể hỏi: 'Tìm tòa nhà Quận 1' hoặc 'Giá thuê bao nhiêu'?";
    }
}