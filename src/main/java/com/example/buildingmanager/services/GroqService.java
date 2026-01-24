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
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String callGroq(String prompt) {
        // 1. Headers (Chuẩn OpenAI)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // 2. Body JSON
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", Collections.singletonList(message));
        requestBody.put("temperature", 0.6); // 0.6 để câu trả lời ổn định

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 3. Gọi API
            Map response = restTemplate.postForObject(apiUrl, entity, Map.class);
            return extractContent(response);

        } catch (HttpClientErrorException e) {
            System.err.println("🔴 Lỗi Groq: " + e.getResponseBodyAsString());
            return "Lỗi Groq (" + e.getStatusCode() + "): " + e.getResponseBodyAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return "AI đang bận, vui lòng thử lại sau.";
        }
    }

    private String extractContent(Map response) {
        try {
            List choices = (List) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                return (String) message.get("content");
            }
            return "AI không phản hồi.";
        } catch (Exception e) {
            return "Lỗi đọc dữ liệu.";
        }
    }
}