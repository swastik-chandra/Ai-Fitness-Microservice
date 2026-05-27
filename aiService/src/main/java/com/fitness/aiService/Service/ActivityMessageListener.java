package com.fitness.aiService.Service;

import com.fitness.aiService.Repository.RecommendationRepository;
import com.fitness.aiService.model.Activity;
import com.fitness.aiService.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {
    private final ActivityAiService activityAiService;
    private final RecommendationRepository recommendationRepository;

    @KafkaListener(topics = "activity.tracked", groupId = "ai-service-group")
    public void processActivity(Activity activity) {
        try {
            log.info("Received activity from Kafka for processing: {}", activity.getId());
            Recommendation recommendation = activityAiService.generateRecommendation(activity);
            recommendationRepository.save(recommendation);
            log.info("Generated and saved recommendation for activity: {}", activity.getId());
        } catch (Exception e) {
            log.error("Error processing activity: {}", activity.getId(), e);
        }
    }
}
