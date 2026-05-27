package com.fitness.aiService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "nutrition_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionPlan {
    @Id
    private String id;
    private String userId;
    private int dailyCalorieTarget;
    private Map<String, Integer> macros; // protein, carbs, fat grams
    private List<Map<String, Object>> meals;
    private String summary;
    private LocalDateTime generatedAt;
}
