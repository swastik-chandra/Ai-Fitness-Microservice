package com.fitness.analyticsService.repository;

import com.fitness.analyticsService.model.UserAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AnalyticsRepository extends MongoRepository<UserAnalytics, String> {
    Optional<UserAnalytics> findByUserId(String userId);
}
