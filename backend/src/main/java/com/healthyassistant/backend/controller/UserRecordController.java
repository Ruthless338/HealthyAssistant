package com.healthyassistant.backend.controller;

import com.healthyassistant.backend.dto.AddRecordRequest;
import com.healthyassistant.backend.model.UserRecord;
import com.healthyassistant.backend.service.UserRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/record")
public class UserRecordController {
    @Autowired
    private UserRecordService userRecordService;

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
}