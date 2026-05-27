package com.fitness.aiService.Controller;

import com.fitness.aiService.Service.WorkoutPlanService;
import com.fitness.aiService.model.WorkoutPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/workout-plan")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @PostMapping("/generate")
    public ResponseEntity<WorkoutPlan> generatePlan(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String goal = request.getOrDefault("goal", "general fitness");
        String level = request.getOrDefault("fitnessLevel", "intermediate");
        return ResponseEntity.ok(workoutPlanService.generatePlan(userId, goal, level));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WorkoutPlan> getLatestPlan(@PathVariable String userId) {
        return ResponseEntity.ok(workoutPlanService.getLatestPlan(userId));
    }
}
