package com.fitness.notificationService.service;

import com.fitness.notificationService.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user.registered", groupId = "notification-service-group")
    public void handleUserRegistered(Map<String, Object> event) {
        try {
            String userId = (String) event.get("userId");
            String firstName = (String) event.get("firstName");
            log.info("Received user.registered event for: {}", userId);

            notificationService.createAndSend(
                    userId,
                    NotificationType.WELCOME,
                    "Welcome to FitAI! 🎉",
                    "Hey " + firstName + "! Welcome to the AI-powered fitness platform. Start logging your workouts to get personalized AI recommendations."
            );
        } catch (Exception e) {
            log.error("Error processing user.registered event: ", e);
        }
    }

    @KafkaListener(topics = "activity.tracked", groupId = "notification-service-group")
    public void handleActivityTracked(Map<String, Object> event) {
        try {
            String userId = (String) event.get("userId");
            String type = event.get("type") != null ? event.get("type").toString() : "WORKOUT";
            Integer calories = event.get("caloriesBurned") != null ? ((Number) event.get("caloriesBurned")).intValue() : 0;
            log.info("Received activity.tracked event for user: {}", userId);

            notificationService.createAndSend(
                    userId,
                    NotificationType.WORKOUT_COMPLETE,
                    "Workout Complete! 💪",
                    "Great job! Your " + type.replace("_", " ").toLowerCase() + " workout has been logged. You burned " + calories + " calories. AI analysis is on its way!"
            );
        } catch (Exception e) {
            log.error("Error processing activity.tracked event: ", e);
        }
    }
}
