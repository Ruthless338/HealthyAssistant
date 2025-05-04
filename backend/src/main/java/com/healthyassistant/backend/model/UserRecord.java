package com.healthyassistant.backend.model;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;

@Entity
@Setter
@Getter
public class UserRecord extends User {
    private Long recordId; // 记录ID

    private String recordDate; // 记录日期
    private int exerciseTime; // 锻炼时间（分钟）
    private String exerciseType; // 锻炼类型
    private String progressNotes; // 进展备注

    // 与User类的关联
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}