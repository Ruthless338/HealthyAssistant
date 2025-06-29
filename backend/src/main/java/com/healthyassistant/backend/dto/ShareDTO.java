package com.healthyassistant.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareDTO {
    private Long id;
    private String title;
    private String content;
    private String authorName; // 只返回作者名称和头像
    private String authorAvatar;
    private List<String> images;
    private List<String> tags; // 新增：分享标签
    private String sportType; // 新增：运动类型
    private String difficultyLevel; // 新增：难度等级
    private String targetAudience; // 新增：适用人群
    private Integer likes;
    private Integer views;
    private Integer comments;
    private LocalDateTime createdAt;
    private Boolean isLiked = false; // 设置默认值为false
    private Double recommendationScore; // 新增：推荐分数

    public boolean getIsLiked() {
        return isLiked != null ? isLiked : false;
    }

    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked != null ? isLiked : false;
    }
}
