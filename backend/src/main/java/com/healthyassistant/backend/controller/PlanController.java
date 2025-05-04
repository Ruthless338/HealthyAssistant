package com.healthyassistant.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.healthyassistant.backend.model.WeekPlan;
import com.healthyassistant.backend.service.DeepSeekAIService;
import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.service.UserService;
import com.healthyassistant.backend.dto.UserRequest;

// PlanController.java
@RestController
@RequestMapping("/api/plan")
public class PlanController {
    @Autowired
    private DeepSeekAIService deepSeekAIService;
    @Autowired
    private UserService userService;

    @PostMapping("/generate")
    public ResponseEntity<?> generatePlan(@RequestBody UserRequest request) {
        try {
            User user = userService.getUserById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            WeekPlan plan = deepSeekAIService.generatePlan(user);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "计划生成失败",
                            "detail", e.getMessage()));
        }
    }
}
