package com.healthyassistant.backend.repository;

import com.healthyassistant.backend.model.VideoTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoTaskRepository extends JpaRepository<VideoTask, Long> {
    List<VideoTask> findByUserIdAndStatus(Long userId, String status);

    List<VideoTask> findByStatus(String status);

    Optional<VideoTask> findByTaskId(String taskId);

    List<VideoTask> findByUserIdAndDayIndexAndExerciseIndex(Long userId, Integer dayIndex, Integer exerciseIndex);
}