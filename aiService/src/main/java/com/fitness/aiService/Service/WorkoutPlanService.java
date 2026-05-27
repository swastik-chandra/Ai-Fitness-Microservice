package com.fitness.aiService.Service;

import com.fitness.aiService.Repository.WorkoutPlanRepository;
import com.fitness.aiService.model.WorkoutPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkoutPlanService {

    private final GeminiService geminiService;
    private final WorkoutPlanRepository workoutPlanRepository;

    public WorkoutPlan generatePlan(String userId, String goal, String fitnessLevel) {
        String prompt = """
                Generate a 7-day workout plan for a user with the following details:
                - Goal: %s
                - Fitness Level: %s
                
                Return ONLY a valid JSON object with this structure:
                {
                  "summary": "Brief description of the plan",
                  "weeklyPlan": [
                    {
                      "day": "Monday",
                      "workout": "Workout name",
                      "exercises": ["exercise 1", "exercise 2"],
                      "duration": 45,
                      "intensity": "Medium"
                    }
                  ]
                }
                
                Include rest days. Consider progressive overload and muscle recovery.
                """.formatted(goal, fitnessLevel);

        try {
            String response = geminiService.getAnswer(prompt);
            log.info("Generated workout plan for user: {}", userId);

            WorkoutPlan plan = WorkoutPlan.builder()
                    .userId(userId)
                    .goal(goal)
                    .weeklyPlan(List.of(Map.of(
                            "rawResponse", response,
                            "goal", goal,
                            "fitnessLevel", fitnessLevel
                    )))
                    .summary("AI-generated " + goal + " plan for " + fitnessLevel + " level")
                    .generatedAt(LocalDateTime.now())
                    .build();

            return workoutPlanRepository.save(plan);
        } catch (Exception e) {
            log.error("Failed to generate workout plan: ", e);
            throw new RuntimeException("Failed to generate workout plan");
        }
    }

    public WorkoutPlan getLatestPlan(String userId) {
        return workoutPlanRepository.findFirstByUserIdOrderByGeneratedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("No workout plan found. Generate one first!"));
    }
}
