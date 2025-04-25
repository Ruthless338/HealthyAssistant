package com.healthyassistant.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Setter;
import lombok.Getter;

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
    private int height;
    private int weight;
    //目标：全身减脂减重、局部变瘦更紧致、增肌塑形提升线条、提升运动能力/成绩、保持健康、康复/疼痛缓解
    private String[] goal; 
    //兴趣：跳舞、跳绳、跑步、健身、瑜伽、游泳、骑行、拳击、徒手训练
    private String[] interest;
    //部位：全身、肩背、腰腹、臀部、手臂、腿部、胸部
    private String[] part;
    private String avatar; // 头像的URL或路径
    private String gender;

}