package com.healthyassistant.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 用户偏好权重（运动类型 -> 权重）
    @ElementCollection
    @CollectionTable(name = "user_sport_preferences", joinColumns = @JoinColumn(name = "user_profile_id"))
    @MapKeyColumn(name = "sport_type")
    @Column(name = "weight")
    private Map<String, Double> sportPreferences = new HashMap<>();

    // 用户偏好权重（难度等级 -> 权重）
    @ElementCollection
    @CollectionTable(name = "user_difficulty_preferences", joinColumns = @JoinColumn(name = "user_profile_id"))
    @MapKeyColumn(name = "difficulty_level")
    @Column(name = "weight")
    private Map<String, Double> difficultyPreferences = new HashMap<>();

    // 用户偏好权重（标签 -> 权重）
    @ElementCollection
    @CollectionTable(name = "user_tag_preferences", joinColumns = @JoinColumn(name = "user_profile_id"))
    @MapKeyColumn(name = "tag")
    @Column(name = "weight")
    private Map<String, Double> tagPreferences = new HashMap<>();

    // 用户行为统计
    private Integer totalLikes = 0;
    private Integer totalViews = 0;
    private Integer totalShares = 0;
    private Integer totalComments = 0;

    // 用户活跃度分数
    private Double activityScore = 0.0;

    // 最后活跃时间
    private LocalDateTime lastActiveTime;

    // 用户兴趣变化趋势
    private String interestTrend; // "increasing", "stable", "decreasing"

    @PrePersist
    protected void onCreate() {
        lastActiveTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastActiveTime = LocalDateTime.now();
    }
}