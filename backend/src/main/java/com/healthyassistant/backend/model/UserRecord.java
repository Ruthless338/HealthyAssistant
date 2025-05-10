package com.healthyassistant.backend.model;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Setter
@Getter
public class UserRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId; // 记录ID

    private int sets; // 组数
    private int durationPerSet; // 每组持续时间（分钟）
    private int restBetweenSets; // 组间休息时间（分钟）
    private String recordDate; // 记录日期
    private String exerciseType; // 锻炼类型
    private String notes; // 自我感受
    private String imageUrl; // 图片URL

    // 与User类的关联
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}