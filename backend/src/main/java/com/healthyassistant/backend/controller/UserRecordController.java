package com.healthyassistant.backend.controller;

import com.healthyassistant.backend.dto.AddRecordRequest;
import com.healthyassistant.backend.model.UserRecord;
import com.healthyassistant.backend.service.UserRecordService;
import com.healthyassistant.backend.service.DeepSeekAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/record")
public class UserRecordController {
    @Autowired
    private UserRecordService userRecordService;
    @Autowired
    private DeepSeekAIService deepSeekAIService;

    // 添加记录
    @PostMapping("/addRecord")
    public UserRecord addRecord(@RequestBody AddRecordRequest addUserRecord) {
        return userRecordService.addRecord(addUserRecord.getUserRecord(), addUserRecord.getUsername());
    }

    // 更新记录
    // @PutMapping("/updateRecord/{recordId}")
    // public UserRecord updateRecord(@PathVariable Long recordId, @RequestBody
    // UserRecord userRecordDetails) {
    // return userRecordService.updateRecord(recordId, userRecordDetails);
    // }

    // 删除记录
    @DeleteMapping("/deleteRecord/{recordId}")
    public void deleteRecord(@PathVariable Long recordId) {
        userRecordService.deleteRecord(recordId);
    }

    // 查看记录
    @GetMapping("/readRecord/{username}")
    public List<UserRecord> readRecords(@PathVariable String username) {
        return userRecordService.readRecordsByUsername(username);
    }

    @PostMapping("/ai-chat")
    public ResponseEntity<?> aiChat(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String userInput = request.get("message");
        List<UserRecord> records = userRecordService.getRecordsByUsername(username);
        StringBuilder prompt = new StringBuilder();
        prompt.append("以下是我的运动记录：\n");
        for (UserRecord r : records) {
            prompt.append(String.format("日期：%s，类型：%s，组数：%d，单组时长：%d秒，间隔：%d秒，感受：%s\n",
                    r.getRecordDate(), r.getExerciseType(), r.getSets(), r.getDurationPerSet(), r.getRestBetweenSets(),
                    r.getNotes()));
        }
        prompt.append("\n我的问题：").append(userInput);
        try {
            String aiReply = deepSeekAIService.chatWithAI(prompt.toString());
            return ResponseEntity.ok(Map.of("reply", aiReply));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "AI对话失败", "detail", e.getMessage()));
        }
    }
}