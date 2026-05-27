package com.fitness.aiService.Service;

import com.fitness.aiService.Repository.RecommendationRepository;
import com.fitness.aiService.model.Activity;
import com.fitness.aiService.model.Recommendation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityMessageListenerTest {

    @Mock
    private ActivityAiService activityAiService;
    @Mock
    private RecommendationRepository recommendationRepository;

    @InjectMocks
    private ActivityMessageListener listener;

    @Test
    @DisplayName("processActivity should generate and save recommendation")
    void processActivity_ShouldGenerateAndSave() {
        Activity activity = new Activity();
        activity.setId("act-1");
        activity.setUserId("user-1");

        Recommendation rec = new Recommendation();
        rec.setId("rec-1");
        when(activityAiService.generateRecommendation(any())).thenReturn(rec);
        when(recommendationRepository.save(any())).thenReturn(rec);

        listener.processActivity(activity);

        verify(activityAiService).generateRecommendation(activity);
        verify(recommendationRepository).save(rec);
    }

    @Test
    @DisplayName("processActivity should handle errors gracefully")
    void processActivity_ShouldHandleErrors() {
        Activity activity = new Activity();
        activity.setId("act-err");

        when(activityAiService.generateRecommendation(any()))
                .thenThrow(new RuntimeException("AI failed"));

        // Should not throw
        listener.processActivity(activity);

        verify(recommendationRepository, never()).save(any());
    }
}
