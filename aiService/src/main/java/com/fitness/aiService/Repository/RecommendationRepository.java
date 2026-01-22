package com.fitness.aiService.Repository;

import com.fitness.aiService.model.Recommendation;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendationRepository extends MongoRepository< Recommendation,String> {

    @Nullable List<Recommendation> findByUserId(String userId);

    @Nullable
    Optional<Recommendation> findByActivityId(String activityId);
}
