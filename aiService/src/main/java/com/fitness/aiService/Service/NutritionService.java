package com.fitness.aiService.Service;

import com.fitness.aiService.Repository.NutritionPlanRepository;
import com.fitness.aiService.model.NutritionPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NutritionService {

    private final GeminiService geminiService;
    private final NutritionPlanRepository nutritionPlanRepository;

    public NutritionPlan generatePlan(String userId, String goal, int targetCalories, String dietaryPreference) {
        String prompt = """
                Generate a daily nutrition plan with the following details:
                - Goal: %s
                - Daily Calorie Target: %d calories
                - Dietary Preference: %s
                
                Return a detailed meal plan with breakfast, lunch, dinner, and 2 snacks.
                Include macro breakdown (protein, carbs, fat in grams).
                Provide specific food items with portions.
                Be practical and include common foods.
                """.formatted(goal, targetCalories, dietaryPreference);

        try {
            String response = geminiService.getAnswer(prompt);
            log.info("Generated nutrition plan for user: {}", userId);

            NutritionPlan plan = NutritionPlan.builder()
                    .userId(userId)
                    .dailyCalorieTarget(targetCalories)
                    .macros(Map.of(
                            "protein", (int) (targetCalories * 0.3 / 4),
                            "carbs", (int) (targetCalories * 0.4 / 4),
                            "fat", (int) (targetCalories * 0.3 / 9)
                    ))
                    .meals(List.of(Map.of("rawResponse", response, "goal", goal)))
                    .summary("AI-generated " + goal + " nutrition plan (" + targetCalories + " cal/day)")
                    .generatedAt(LocalDateTime.now())
                    .build();

            return nutritionPlanRepository.save(plan);
        } catch (Exception e) {
            log.error("Failed to generate nutrition plan: ", e);
            throw new RuntimeException("Failed to generate nutrition plan");
        }
    }

    public NutritionPlan getLatestPlan(String userId) {
        return nutritionPlanRepository.findFirstByUserIdOrderByGeneratedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("No nutrition plan found. Generate one first!"));
    }
}
