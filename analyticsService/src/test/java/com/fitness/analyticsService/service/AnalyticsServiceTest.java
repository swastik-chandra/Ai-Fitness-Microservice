package com.fitness.analyticsService.service;

import com.fitness.analyticsService.model.UserAnalytics;
import com.fitness.analyticsService.repository.AnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository repository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private UserAnalytics existingAnalytics;

    @BeforeEach
    void setUp() {
        existingAnalytics = UserAnalytics.builder()
                .userId("user-123")
                .totalWorkouts(5)
                .totalCalories(1500)
                .totalDuration(150)
                .currentStreak(3)
                .longestStreak(5)
                .build();
    }

    @Test
    @DisplayName("processActivity should update existing analytics")
    void processActivity_ShouldUpdateExistingAnalytics() {
        when(repository.findByUserId("user-123")).thenReturn(Optional.of(existingAnalytics));
        when(repository.save(any(UserAnalytics.class))).thenReturn(existingAnalytics);

        analyticsService.processActivity("user-123", "RUNNING", 300, 30);

        verify(repository).save(argThat(analytics -> {
            assertEquals(6, analytics.getTotalWorkouts());
            assertEquals(1800, analytics.getTotalCalories());
            assertEquals(180, analytics.getTotalDuration());
            return true;
        }));
    }

    @Test
    @DisplayName("processActivity should create new analytics for new user")
    void processActivity_ShouldCreateNewAnalytics() {
        when(repository.findByUserId("new-user")).thenReturn(Optional.empty());
        when(repository.save(any(UserAnalytics.class))).thenAnswer(inv -> inv.getArgument(0));

        analyticsService.processActivity("new-user", "YOGA", 150, 60);

        verify(repository).save(argThat(analytics -> {
            assertEquals("new-user", analytics.getUserId());
            assertEquals(1, analytics.getTotalWorkouts());
            assertEquals(150, analytics.getTotalCalories());
            return true;
        }));
    }

    @Test
    @DisplayName("getUserSummary should return correct stats")
    void getUserSummary_ShouldReturnCorrectStats() {
        when(repository.findByUserId("user-123")).thenReturn(Optional.of(existingAnalytics));

        Map<String, Object> summary = analyticsService.getUserSummary("user-123");

        assertEquals(5, summary.get("totalWorkouts"));
        assertEquals(1500, summary.get("totalCalories"));
        assertEquals(150, summary.get("totalDuration"));
        assertEquals(300, summary.get("averageCaloriesPerWorkout"));
    }

    @Test
    @DisplayName("getUserSummary should handle missing user")
    void getUserSummary_ShouldHandleMissingUser() {
        when(repository.findByUserId("unknown")).thenReturn(Optional.empty());

        Map<String, Object> summary = analyticsService.getUserSummary("unknown");

        assertEquals(0, summary.get("totalWorkouts"));
        assertEquals(0, summary.get("totalCalories"));
    }
}
