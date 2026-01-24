package com.example.buildingmanager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String callGemini(String prompt) {
        // 1. URL + Key
        String finalUrl = apiUrl + "?key=" + apiKey;

        // 2. Body JSON chuẩn Google
        // { "contents": [{ "parts": [{ "text": "..." }] }] }
        Map<String, Object> contentPart = new HashMap<>();
        contentPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(contentPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(content));

        // 3. Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 4. Gọi API
            Map response = restTemplate.postForObject(finalUrl, entity, Map.class);
            return extractTextFromResponse(response);
            
        } catch (HttpClientErrorException e) {
            // In lỗi chi tiết ra Console để debug nếu Google từ chối
            System.err.println("🔴 LỖI GEMINI: " + e.getResponseBodyAsString());
            return "Lỗi kết nối AI: " + e.getStatusText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Hệ thống đang bận, vui lòng thử lại sau.";
        }
    }

    private String extractTextFromResponse(Map response) {
        try {
            List candidates = (List) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map firstCandidate = (Map) candidates.get(0);
                Map content = (Map) firstCandidate.get("content");
                List parts = (List) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    Map firstPart = (Map) parts.get(0);
                    return (String) firstPart.get("text");
                }
            }
            return "AI không phản hồi.";
        } catch (Exception e) {
            return "Lỗi đọc dữ liệu.";
        }
    }
}