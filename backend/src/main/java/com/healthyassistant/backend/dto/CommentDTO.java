package com.healthyassistant.backend.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDTO {
    private Long id;
    private String content;
    private String authorName;
    private String authorAvatar;
    private LocalDateTime createdAt;
    private Long shareId;
}