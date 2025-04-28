package com.healthyassistant.backend.service;

import com.healthyassistant.backend.model.UserRecord;
import com.healthyassistant.backend.repository.UserRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRecordService {
    @Autowired
    private UserRecordRepository userRecordRepository;

    // 添加记录
    public UserRecord addRecord(UserRecord userRecord) {
        return userRecordRepository.save(userRecord);
    }

    // 更新记录
    public UserRecord updateRecord(Long recordId, UserRecord userRecordDetails) {
        UserRecord userRecord = userRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found with id: " + recordId));

        userRecord.setRecordDate(userRecordDetails.getRecordDate());
        userRecord.setWeight(userRecordDetails.getWeight());
        userRecord.setExerciseTime(userRecordDetails.getExerciseTime());
        userRecord.setExerciseType(userRecordDetails.getExerciseType());
        userRecord.setProgressNotes(userRecordDetails.getProgressNotes());

        return userRecordRepository.save(userRecord);
    }

    // 删除记录
    public void deleteRecord(Long recordId) {
        UserRecord userRecord = userRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found with id: " + recordId));
        userRecordRepository.delete(userRecord);
    }

    // 查看记录
    public List<UserRecord> readRecordsByUsername(String username) {
        return userRecordRepository.findByUsername(username);
    }
}