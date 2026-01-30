package com.fitness.aiService.Service;

import com.fitness.aiService.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {
    private final ActivityAiService aiService;

    @RabbitListener(queues = "activity.queue" )
    public void processActivity(Activity activity){
        log.info("Received Activity For Processing :{}", activity.getId());
        log.info("Generated Recommendation :{}", aiService.generateRecommendation(activity));

    }
}
