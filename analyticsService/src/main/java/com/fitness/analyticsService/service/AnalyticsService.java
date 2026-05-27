package com.fitness.analyticsService.service;

import com.fitness.analyticsService.model.DailyStats;
import com.fitness.analyticsService.model.UserAnalytics;
import com.fitness.analyticsService.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository repository;

    @CacheEvict(value = {"analytics-summary", "analytics-weekly"}, key = "#userId")
    public void processActivity(String userId, String activityType, int calories, int duration) {
        UserAnalytics analytics = repository.findByUserId(userId)
                .orElse(UserAnalytics.builder().userId(userId).build());

        // Update totals
        analytics.setTotalWorkouts(analytics.getTotalWorkouts() + 1);
        analytics.setTotalCalories(analytics.getTotalCalories() + calories);
        analytics.setTotalDuration(analytics.getTotalDuration() + duration);

        // Update type breakdown
        Map<String, Integer> byType = analytics.getWorkoutsByType();
        if (byType == null) byType = new HashMap<>();
        byType.merge(activityType, 1, Integer::sum);
        analytics.setWorkoutsByType(byType);

        // Update daily stats
        LocalDate today = LocalDate.now();
        List<DailyStats> dailyStats = analytics.getDailyStats();
        if (dailyStats == null) dailyStats = new ArrayList<>();

        Optional<DailyStats> todayStats = dailyStats.stream()
                .filter(ds -> ds.getDate().equals(today))
                .findFirst();

        if (todayStats.isPresent()) {
            DailyStats ds = todayStats.get();
            ds.setTotalCalories(ds.getTotalCalories() + calories);
            ds.setTotalDuration(ds.getTotalDuration() + duration);
            ds.setWorkoutCount(ds.getWorkoutCount() + 1);
        } else {
            dailyStats.add(DailyStats.builder()
                    .date(today)
                    .totalCalories(calories)
                    .totalDuration(duration)
                    .workoutCount(1)
                    .mostActiveType(activityType)
                    .build());
        }

        // Keep only last 90 days
        LocalDate cutoff = today.minusDays(90);
        dailyStats = dailyStats.stream()
                .filter(ds -> ds.getDate().isAfter(cutoff))
                .collect(Collectors.toList());
        analytics.setDailyStats(dailyStats);

        // Update streaks
        updateStreaks(analytics, today);

        analytics.setLastActivityDate(LocalDateTime.now());
        analytics.setUpdatedAt(LocalDateTime.now());
        repository.save(analytics);
        log.info("Updated analytics for user: {}", userId);
    }

    @Cacheable(value = "analytics-summary", key = "#userId")
    public Map<String, Object> getUserSummary(String userId) {
        UserAnalytics analytics = repository.findByUserId(userId)
                .orElse(UserAnalytics.builder().userId(userId).build());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalWorkouts", analytics.getTotalWorkouts());
        summary.put("totalCalories", analytics.getTotalCalories());
        summary.put("totalDuration", analytics.getTotalDuration());
        summary.put("currentStreak", analytics.getCurrentStreak());
        summary.put("longestStreak", analytics.getLongestStreak());
        summary.put("workoutsByType", analytics.getWorkoutsByType());
        summary.put("averageCaloriesPerWorkout",
                analytics.getTotalWorkouts() > 0
                        ? analytics.getTotalCalories() / analytics.getTotalWorkouts() : 0);
        summary.put("averageDurationPerWorkout",
                analytics.getTotalWorkouts() > 0
                        ? analytics.getTotalDuration() / analytics.getTotalWorkouts() : 0);
        return summary;
    }

    @Cacheable(value = "analytics-weekly", key = "#userId")
    public List<DailyStats> getWeeklyStats(String userId) {
        UserAnalytics analytics = repository.findByUserId(userId).orElse(null);
        if (analytics == null || analytics.getDailyStats() == null) return List.of();

        LocalDate weekAgo = LocalDate.now().minusDays(7);
        return analytics.getDailyStats().stream()
                .filter(ds -> ds.getDate().isAfter(weekAgo))
                .sorted(Comparator.comparing(DailyStats::getDate))
                .collect(Collectors.toList());
    }

    public List<DailyStats> getMonthlyStats(String userId) {
        UserAnalytics analytics = repository.findByUserId(userId).orElse(null);
        if (analytics == null || analytics.getDailyStats() == null) return List.of();

        LocalDate monthAgo = LocalDate.now().minusDays(30);
        return analytics.getDailyStats().stream()
                .filter(ds -> ds.getDate().isAfter(monthAgo))
                .sorted(Comparator.comparing(DailyStats::getDate))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStreaks(String userId) {
        UserAnalytics analytics = repository.findByUserId(userId)
                .orElse(UserAnalytics.builder().userId(userId).build());

        return Map.of(
                "currentStreak", analytics.getCurrentStreak(),
                "longestStreak", analytics.getLongestStreak(),
                "lastActivityDate", analytics.getLastActivityDate() != null
                        ? analytics.getLastActivityDate().toString() : "Never"
        );
    }

    private void updateStreaks(UserAnalytics analytics, LocalDate today) {
        List<DailyStats> stats = analytics.getDailyStats();
        if (stats == null || stats.isEmpty()) {
            analytics.setCurrentStreak(1);
            analytics.setLongestStreak(1);
            return;
        }

        Set<LocalDate> activeDates = stats.stream()
                .map(DailyStats::getDate)
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate check = today;
        while (activeDates.contains(check)) {
            streak++;
            check = check.minusDays(1);
        }
        analytics.setCurrentStreak(streak);
        if (streak > analytics.getLongestStreak()) {
            analytics.setLongestStreak(streak);
        }
    }
}
