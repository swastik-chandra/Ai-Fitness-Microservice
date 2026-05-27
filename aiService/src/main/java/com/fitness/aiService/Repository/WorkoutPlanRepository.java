package com.fitness.aiService.Repository;

import com.fitness.aiService.model.WorkoutPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WorkoutPlanRepository extends MongoRepository<WorkoutPlan, String> {
    Optional<WorkoutPlan> findFirstByUserIdOrderByGeneratedAtDesc(String userId);
}
