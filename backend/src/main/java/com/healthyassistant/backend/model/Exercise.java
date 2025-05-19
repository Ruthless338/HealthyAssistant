package com.healthyassistant.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "exercise")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 添加主键字段

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @Column(name = "exercise_groups")
    @JsonProperty("groups")
    private int groups;

    @Column(name = "exercise_interval")
    @JsonProperty("interval")
    private double interval;

    @JsonProperty("image")
    private String image;

    // 处理可能的空值
    public String getImage() {
        return image != null ? image : "https://example.com/default-image.png"; // 确保链接是有效的
    }
}