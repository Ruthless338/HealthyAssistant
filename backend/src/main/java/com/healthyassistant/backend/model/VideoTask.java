package com.healthyassistant.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "video_task")
public class VideoTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("task_id")
    private String taskId;

    @JsonProperty("exercise_name")
    private String exerciseName;

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("status")
    private String status; // PROCESSING, SUCCESS, FAILED

    @JsonProperty("video_url")
    private String videoUrl;

    @JsonProperty("local_path")
    private String localPath;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("day_index")
    private Integer dayIndex;

    @JsonProperty("exercise_index")
    private Integer exerciseIndex;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}