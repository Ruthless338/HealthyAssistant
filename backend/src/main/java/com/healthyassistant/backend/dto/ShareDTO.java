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
    private String authorName; // 只返回作者名称
    private List<String> images;
    private Integer likes;
    private Integer views;
    private LocalDateTime createdAt;

    private boolean isLiked;

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }
}
