package com.fitness.analyticsService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "user_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalytics {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    @Builder.Default
    private int totalWorkouts = 0;
    @Builder.Default
    private int totalCalories = 0;
    @Builder.Default
    private int totalDuration = 0;
    @Builder.Default
    private int currentStreak = 0;
    @Builder.Default
    private int longestStreak = 0;

    @Builder.Default
    private Map<String, Integer> workoutsByType = new HashMap<>();

    @Builder.Default
    private List<DailyStats> dailyStats = new ArrayList<>();

    private LocalDateTime lastActivityDate;
    private LocalDateTime updatedAt;
}
