package com.fitness.analyticsService.controller;

import com.fitness.analyticsService.model.DailyStats;
import com.fitness.analyticsService.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getUserSummary(@PathVariable String userId) {
        return ResponseEntity.ok(analyticsService.getUserSummary(userId));
    }

    @GetMapping("/user/{userId}/weekly")
    public ResponseEntity<List<DailyStats>> getWeeklyStats(@PathVariable String userId) {
        return ResponseEntity.ok(analyticsService.getWeeklyStats(userId));
    }

    @GetMapping("/user/{userId}/monthly")
    public ResponseEntity<List<DailyStats>> getMonthlyStats(@PathVariable String userId) {
        return ResponseEntity.ok(analyticsService.getMonthlyStats(userId));
    }

    @GetMapping("/user/{userId}/streaks")
    public ResponseEntity<Map<String, Object>> getStreaks(@PathVariable String userId) {
        return ResponseEntity.ok(analyticsService.getStreaks(userId));
    }
}
