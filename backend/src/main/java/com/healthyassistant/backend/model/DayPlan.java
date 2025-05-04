package com.healthyassistant.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity // 添加@Entity注解
@Getter
@Setter
public class DayPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 添加主键字段

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "day_plan_id") // 关联字段名
    private List<Exercise> exercises; // 原字段保持不变
}