package com.healthyassistant.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthyassistant.backend.model.VideoTask;
import com.healthyassistant.backend.repository.VideoTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@Service
public class VideoGenerationService {

    @Autowired
    private VideoTaskRepository videoTaskRepository;

    @Value("${zhipuai.api.key}")
    private String apiKey;

    @Value("${zhipuai.api.url:https://open.bigmodel.cn/api/paas/v4/videos/generations}")
    private String apiUrl;

    @Value("${video.storage.path:./videos}")
    private String videoStoragePath;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public VideoGenerationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();

        // 设置超时时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(300000); // 5分钟超时
        this.restTemplate.setRequestFactory(factory);
    }

    /**
     * 为运动动作生成视频
     */
    public VideoTask generateExerciseVideo(Long userId, String exerciseName, String description,
            Integer dayIndex, Integer exerciseIndex) {
        try {
            // 构建提示词
            String prompt = buildExercisePrompt(exerciseName, description);

            // 调用智谱清言API
            String taskId = callZhipuaiAPI(prompt);

            // 创建视频任务记录
            VideoTask videoTask = new VideoTask();
            videoTask.setTaskId(taskId);
            videoTask.setExerciseName(exerciseName);
            videoTask.setPrompt(prompt);
            videoTask.setStatus("PROCESSING");
            videoTask.setUserId(userId);
            videoTask.setDayIndex(dayIndex);
            videoTask.setExerciseIndex(exerciseIndex);

            return videoTaskRepository.save(videoTask);

        } catch (Exception e) {
            throw new RuntimeException("视频生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用智谱清言API
     */
    private String callZhipuaiAPI(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "cogvideox-flash");
        requestBody.put("prompt", prompt);
        requestBody.put("quality", "quality");
        requestBody.put("with_audio", true);
        requestBody.put("size", "1920x1080");
        requestBody.put("fps", 30);

        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode responseNode = objectMapper.readTree(response.getBody());
            String taskStatus = responseNode.path("task_status").asText();
            String taskId = responseNode.path("id").asText();

            if ("PROCESSING".equals(taskStatus) || "SUCCESS".equals(taskStatus)) {
                return taskId;
            } else {
                throw new RuntimeException("视频生成任务创建失败: " + taskStatus);
            }
        } else {
            throw new RuntimeException("API调用失败: " + response.getStatusCode());
        }
    }

    /**
     * 构建运动动作的提示词
     */
    private String buildExercisePrompt(String exerciseName, String description) {
        return String.format(
                "请生成一个专业的健身动作示范视频，动作名称：%s。动作要点：%s。" +
                        "要求：1. 视频中展示标准的动作要领和正确的姿势；" +
                        "2. 动作要清晰流畅，适合初学者学习；" +
                        "3. 背景简洁，突出动作细节；" +
                        "4. 视频时长控制在10-15秒；" +
                        "5. 动作要体现健身的专业性和安全性。",
                exerciseName, description);
    }

    /**
     * 检查视频生成状态
     */
    public VideoTask checkVideoStatus(String taskId) {
        try {
            VideoTask videoTask = videoTaskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new RuntimeException("任务不存在"));
            System.out.println(videoTask.getTaskId());

            if ("PROCESSING".equals(videoTask.getStatus())) {
                // 调用API检查状态
                String status = checkZhipuaiTaskStatus(taskId);
                System.out.println(status);
                videoTask.setStatus(status);

                if ("SUCCESS".equals(status)) {
                    // 下载视频
                    String videoUrl = getVideoUrl(taskId);
                    String localPath = downloadVideo(videoUrl, videoTask.getUserId());
                    videoTask.setVideoUrl(videoUrl);
                    videoTask.setLocalPath(localPath);
                }

                videoTaskRepository.save(videoTask);
            }
            System.out.println(videoTask.getTaskId());

            return videoTask;

        } catch (Exception e) {
            throw new RuntimeException("检查视频状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查智谱清言任务状态
     */
    private String checkZhipuaiTaskStatus(String taskId) throws Exception {
        String checkUrl = "https://open.bigmodel.cn/api/paas/v4/async-result/" + taskId;
        System.out.println(checkUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(checkUrl, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode responseNode = objectMapper.readTree(response.getBody());
            return responseNode.path("task_status").asText();
        } else {
            throw new RuntimeException("检查任务状态失败: " + response.getStatusCode());
        }
    }

    /**
     * 获取视频URL
     */
    private String getVideoUrl(String taskId) throws Exception {
        String checkUrl = "https://open.bigmodel.cn/api/paas/v4/videos/generations/" + taskId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(checkUrl, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode responseNode = objectMapper.readTree(response.getBody());
            JsonNode videoResult = responseNode.path("video_result");
            if (videoResult.isArray() && videoResult.size() > 0) {
                return videoResult.get(0).path("url").asText();
            }
        }

        throw new RuntimeException("获取视频URL失败");
    }

    /**
     * 下载视频到本地
     */
    private String downloadVideo(String videoUrl, Long userId) throws IOException {
        // 创建用户视频目录
        String userVideoPath = videoStoragePath + "/" + userId;
        Path userPath = Paths.get(userVideoPath);
        if (!Files.exists(userPath)) {
            Files.createDirectories(userPath);
        }

        // 生成文件名
        String fileName = UUID.randomUUID().toString() + ".mp4";
        String filePath = userVideoPath + "/" + fileName;

        // 下载视频
        URL url = new URL(videoUrl);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            java.io.InputStream is = url.openStream();
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return filePath;
    }

    /**
     * 获取用户的视频任务
     */
    public List<VideoTask> getUserVideoTasks(Long userId) {
        return videoTaskRepository.findByUserIdAndStatus(userId, "SUCCESS");
    }

    /**
     * 获取特定动作的视频任务
     */
    public VideoTask getExerciseVideoTask(Long userId, Integer dayIndex, Integer exerciseIndex) {
        List<VideoTask> tasks = videoTaskRepository.findByUserIdAndDayIndexAndExerciseIndex(userId, dayIndex,
                exerciseIndex);
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    /**
     * 定时检查所有处理中的视频任务
     */
    @Scheduled(fixedRate = 30000) // 每30秒检查一次
    public void checkAllProcessingTasks() {
        List<VideoTask> processingTasks = videoTaskRepository.findByStatus("PROCESSING");

        for (VideoTask task : processingTasks) {
            try {
                checkVideoStatus(task.getTaskId());
            } catch (Exception e) {
                System.err.println("检查任务状态失败: " + task.getTaskId() + ", 错误: " + e.getMessage());
            }
        }
    }
}