package com.fitness.aiService.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;


@Data


public class Activity {

    private String Id;
    private String userId;
    private String type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMatrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
