package com.healthyassistant.backend.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
public class UserDiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Date startDate;
    private Date endDate;

    // 每日饮食计划
    private String[] dailyMeals; // 例如: ["早餐: 鸡蛋2个、牛奶250ml", "午餐: 米饭100g、鸡胸肉150g", ...]

    // 每日营养成分
    private double dailyCalories;
    private double dailyProtein;
    private double dailyCarbohydrates;
    private double dailyFat;

    // 构造函数、getter和setter

    public UserDiet(String username, Date startDate, Date endDate, String[] dailyMeals) {
        this.username = username;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyMeals = dailyMeals;
    }

    // Getters and Setters
}