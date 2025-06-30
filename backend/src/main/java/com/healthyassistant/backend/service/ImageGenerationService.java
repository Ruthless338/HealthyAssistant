package com.healthyassistant.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageGenerationService {
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/images/generations";
    private static final String API_KEY = "ddf973da29504d388c8f3d4a3f03793b.aJUkUF86x9zhDTju";
    private static final String IMAGE_SAVE_DIR = "./uploads/plan-images/";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateImage(String prompt) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "cogview-3-flash");
            requestBody.put("prompt", prompt);
            requestBody.put("n", 1);
            requestBody.put("size", "1024x1024");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String imageUrl = root.path("data").get(0).path("url").asText();
                if (imageUrl == null || imageUrl.isEmpty()) {
                    throw new RuntimeException("未获取到图片URL");
                }
                return downloadAndSaveImage(imageUrl);
            } else {
                throw new RuntimeException("图片生成API调用失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("图片生成失败: " + e.getMessage(), e);
        }
    }

    private String downloadAndSaveImage(String imageUrl) throws Exception {
        Files.createDirectories(Paths.get(IMAGE_SAVE_DIR));
        String fileName = UUID.randomUUID().toString() + ".png";
        String filePath = IMAGE_SAVE_DIR + fileName;
        try (InputStream in = new URL(imageUrl).openStream();
                FileOutputStream out = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        // 返回前端可访问的静态资源路径
        return "/uploads/plan-images/" + fileName;
    }
}