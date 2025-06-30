package com.healthyassistant.backend.controller;

import com.healthyassistant.backend.model.VideoTask;
import com.healthyassistant.backend.service.VideoGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoGenerationService videoGenerationService;

    /**
     * 生成运动动作视频
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateVideo(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String exerciseName = request.get("exerciseName").toString();
            String description = request.get("description").toString();
            Integer dayIndex = Integer.valueOf(request.get("dayIndex").toString());
            Integer exerciseIndex = Integer.valueOf(request.get("exerciseIndex").toString());

            VideoTask videoTask = videoGenerationService.generateExerciseVideo(
                    userId, exerciseName, description, dayIndex, exerciseIndex);

            return ResponseEntity.ok(Map.of(
                    "message", "视频生成任务已创建",
                    "taskId", videoTask.getTaskId(),
                    "status", videoTask.getStatus()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "视频生成失败",
                    "detail", e.getMessage()));
        }
    }

    /**
     * 检查视频生成状态
     */
    @GetMapping("/status/{taskId}")
    public ResponseEntity<?> checkVideoStatus(@PathVariable String taskId) {
        try {
            // System.out.println("checkVideoStatus");
            // System.out.println(taskId);
            VideoTask videoTask = videoGenerationService.checkVideoStatus(taskId);

            Map<String, Object> response = Map.of(
                    "taskId", videoTask.getTaskId(),
                    "status", videoTask.getStatus(),
                    "exerciseName", videoTask.getExerciseName());

            if ("SUCCESS".equals(videoTask.getStatus())) {
                response = Map.of(
                        "taskId", videoTask.getTaskId(),
                        "status", videoTask.getStatus(),
                        "exerciseName", videoTask.getExerciseName(),
                        "videoUrl", videoTask.getVideoUrl(),
                        "localPath", videoTask.getLocalPath());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "检查状态失败",
                    "detail", e.getMessage()));
        }
    }

    /**
     * 获取用户的视频任务列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserVideos(@PathVariable Long userId) {
        try {
            List<VideoTask> videoTasks = videoGenerationService.getUserVideoTasks(userId);
            return ResponseEntity.ok(videoTasks);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "获取视频列表失败",
                    "detail", e.getMessage()));
        }
    }

    /**
     * 获取特定动作的视频
     */
    @GetMapping("/exercise")
    public ResponseEntity<?> getExerciseVideo(
            @RequestParam Long userId,
            @RequestParam Integer dayIndex,
            @RequestParam Integer exerciseIndex) {
        try {
            VideoTask videoTask = videoGenerationService.getExerciseVideoTask(userId, dayIndex, exerciseIndex);

            if (videoTask == null) {
                return ResponseEntity.ok(Map.of(
                        "message", "未找到该动作的视频",
                        "hasVideo", false));
            }

            return ResponseEntity.ok(Map.of(
                    "hasVideo", true,
                    "taskId", videoTask.getTaskId(),
                    "status", videoTask.getStatus(),
                    "videoUrl", videoTask.getVideoUrl(),
                    "localPath", videoTask.getLocalPath()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "获取动作视频失败",
                    "detail", e.getMessage()));
        }
    }

    // 辅助方法：创建Map
    private Map<String, Object> Map(String key, Object value) {
        return java.util.Map.of(key, value);
    }

    private Map<String, Object> Map(String key1, Object value1, String key2, Object value2) {
        return java.util.Map.of(key1, value1, key2, value2);
    }

    private Map<String, Object> Map(String key1, Object value1, String key2, Object value2,
            String key3, Object value3) {
        return java.util.Map.of(key1, value1, key2, value2, key3, value3);
    }

    private Map<String, Object> Map(String key1, Object value1, String key2, Object value2,
            String key3, Object value3, String key4, Object value4) {
        return java.util.Map.of(key1, value1, key2, value2, key3, value3, key4, value4);
    }

    private Map<String, Object> Map(String key1, Object value1, String key2, Object value2,
            String key3, Object value3, String key4, Object value4,
            String key5, Object value5) {
        return java.util.Map.of(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5);
    }
}