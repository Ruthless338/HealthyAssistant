package com.healthyassistant.backend.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import jakarta.persistence.CascadeType;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity // 表示该类是一个实体类
@Setter // 自动生成setter方法
@Getter // 自动生成getter方法
public class User {
    @Id // 表示该字段是主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增策略
    private Long id;
    private String username;
    private String password;
    private int age;
    private double height;
    private double weight;
    // 目标：全身减脂减重、局部变瘦更紧致、增肌塑形提升线条、提升运动能力/成绩、保持健康、康复/疼痛缓解
    @ElementCollection
    private List<String> goal;
    // 兴趣：跳舞、跳绳、跑步、健身、瑜伽、游泳、骑行、拳击、徒手训练
    @ElementCollection
    private List<String> interest;
    // 部位：全身、肩背、腰腹、臀部、手臂、腿部、胸部
    @ElementCollection
    private List<String> part;
    private String avatar; // 头像的URL或路径
    private String gender;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserRecord> userRecords;

    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", age=" + age +
                ", height=" + height +
                ", weight=" + weight +
                ", goal=" + goal +
                ", interest=" + interest +
                ", part=" + part +
                ", avatar='" + avatar + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}