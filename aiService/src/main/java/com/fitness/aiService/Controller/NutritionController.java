package com.fitness.aiService.Controller;

import com.fitness.aiService.Service.NutritionService;
import com.fitness.aiService.model.NutritionPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;

    @PostMapping("/generate")
    public ResponseEntity<NutritionPlan> generatePlan(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String goal = (String) request.getOrDefault("goal", "maintain weight");
        int targetCalories = request.get("targetCalories") != null
                ? ((Number) request.get("targetCalories")).intValue() : 2000;
        String preference = (String) request.getOrDefault("dietaryPreference", "balanced");
        return ResponseEntity.ok(nutritionService.generatePlan(userId, goal, targetCalories, preference));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<NutritionPlan> getLatestPlan(@PathVariable String userId) {
        return ResponseEntity.ok(nutritionService.getLatestPlan(userId));
    }
}
