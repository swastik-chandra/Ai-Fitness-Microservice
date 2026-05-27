package com.fitness.analyticsService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStats {
    private LocalDate date;
    private int totalCalories;
    private int totalDuration;
    private int workoutCount;
    private String mostActiveType;
}
