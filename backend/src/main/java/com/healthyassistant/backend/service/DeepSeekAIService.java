// DeepSeekAIService.java
package com.healthyassistant.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.model.WeekPlan;

@Service
public class DeepSeekAIService {
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${deepseek.api.key}")
    private String apiKey;

    public WeekPlan generatePlan(User user) throws Exception {
        // 构造prompt
        String prompt = buildStructuredPrompt(user);

        // 构造请求体
        String requestBody = String.format("""
                {
                    "model": "deepseek-chat",
                    "messages": [
                        {"role": "user", "content": "%s"}
                    ],
                    "response_format": {"type": "json_object"}
                }
                """, prompt.replace("\"", "\\\""));

        // 发送请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        // 处理响应
        if (response.statusCode() == 200) {
            return parseResponse(response.body());
        } else {
            throw new RuntimeException("API调用失败: " + response.body());
        }
    }

    private String buildStructuredPrompt(User user) {
        return String.format("""
                    请为以下用户生成7天的健身计划，要求：
                    1. 使用严格JSON格式返回，数据结构需符合：
                    {
                        "weekPlan": [
                            {
                                "exercises": [
                                    {
                                        "name": "动作名称",
                                        "description": "动作详细说明",
                                        "group": 3,
                                        "interval": 60,
                                        "image": "图片URL"
                                    }
                                ]
                            }
                        ]
                    }
                    2. 用户信息：
                        - 年龄：%d岁
                        - 性别：%s
                        - 身高：%scm
                        - 体重：%skg
                        - 目标：%s
                        - 兴趣：%s
                        - 锻炼部位：%s
                    }
                }
                    3. 每天包含3-5个训练动作
                    4. 图片URL请生成健身动作示意图链接
                    5. 组间间隔单位为秒
                    """,
                user.getAge(),
                user.getGender(),
                user.getHeight(),
                user.getWeight(),
                String.join(",", user.getGoal()),
                String.join(",", user.getInterest()),
                String.join(",", user.getPart()));
    }

    private WeekPlan parseResponse(String jsonResponse) throws Exception {
        // 提取核心JSON内容
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        String content = rootNode.path("choices").get(0).path("message").path("content").asText();

        // 验证数据结构
        if (!isValidPlan(content)) {
            throw new RuntimeException("API返回数据结构不符合预期");
        }

        return objectMapper.readValue(content, WeekPlan.class);
    }

    private boolean isValidPlan(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.has("weekPlan") &&
                    node.get("weekPlan").isArray() &&
                    node.get("weekPlan").size() == 7;
        } catch (Exception e) {
            return false;
        }
    }
}