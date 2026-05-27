package com.fitness.analyticsService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsEventListener {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "activity.tracked", groupId = "analytics-service-group")
    public void handleActivityTracked(Map<String, Object> event) {
        try {
            String userId = (String) event.get("userId");
            String type = event.get("type") != null ? event.get("type").toString() : "OTHER";
            int calories = event.get("caloriesBurned") != null ? ((Number) event.get("caloriesBurned")).intValue() : 0;
            int duration = event.get("duration") != null ? ((Number) event.get("duration")).intValue() : 0;

            log.info("Analytics: processing activity for user {}, type={}, calories={}, duration={}",
                    userId, type, calories, duration);

            analyticsService.processActivity(userId, type, calories, duration);
        } catch (Exception e) {
            log.error("Error processing activity event for analytics: ", e);
        }
    }
}
