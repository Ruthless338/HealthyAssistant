package com.healthyassistant.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.healthyassistant.backend.model.WeekPlan;
import com.healthyassistant.backend.service.DeepSeekAIService;
import com.healthyassistant.backend.service.VideoGenerationService;
import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.service.UserService;

// PlanController.java
@RestController
@RequestMapping("/api/plan")
public class PlanController {
    @Autowired
    private DeepSeekAIService deepSeekAIService;
    @Autowired
    private UserService userService;
    @Autowired
    private VideoGenerationService videoGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<?> generatePlan(@RequestParam Long id) {
        try {
            System.out.println("Generating plan for user with ID: " + id);
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            // 检查是否已有计划且用户信息未变更
            if (user.getCurrentPlan() != null && !user.isProfileModified()) {
                return ResponseEntity.ok(user.getCurrentPlan());
            }

            WeekPlan plan = deepSeekAIService.generatePlan(user);
            user.setCurrentPlan(plan);
            user.setProfileModified(false);
            userService.save(user);

            // 异步生成所有动作的视频
            generateVideosForPlan(user.getId(), plan);

            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "计划生成失败",
                            "detail", e.getMessage()));
        }
    }

    /**
     * 为计划中的所有动作生成视频
     */
    private void generateVideosForPlan(Long userId, WeekPlan plan) {
        try {
            for (int dayIndex = 0; dayIndex < plan.getWeekPlan().size(); dayIndex++) {
                var day = plan.getWeekPlan().get(dayIndex);
                for (int exerciseIndex = 0; exerciseIndex < day.getExercises().size(); exerciseIndex++) {
                    var exercise = day.getExercises().get(exerciseIndex);

                    final int finalDayIndex = dayIndex;
                    final int finalExerciseIndex = exerciseIndex;
                    final var finalExercise = exercise;

                    // 异步生成视频
                    new Thread(() -> {
                        try {
                            videoGenerationService.generateExerciseVideo(
                                    userId,
                                    finalExercise.getName(),
                                    finalExercise.getDescription(),
                                    finalDayIndex,
                                    finalExerciseIndex);
                        } catch (Exception e) {
                            System.err.println("生成视频失败: " + exercise.getName() + ", 错误: " + e.getMessage());
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            System.err.println("批量生成视频失败: " + e.getMessage());
        }
    }
}
