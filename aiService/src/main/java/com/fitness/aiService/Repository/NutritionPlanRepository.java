package com.fitness.aiService.Repository;

import com.fitness.aiService.model.NutritionPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface NutritionPlanRepository extends MongoRepository<NutritionPlan, String> {
    Optional<NutritionPlan> findFirstByUserIdOrderByGeneratedAtDesc(String userId);
}
