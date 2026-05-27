package com.fitness.activityService.Service;

import com.fitness.activityService.Model.Activity;
import com.fitness.activityService.Model.ActivityType;
import com.fitness.activityService.dto.ActivityRequest;
import com.fitness.activityService.dto.ActivityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private UserValidationService userValidationService;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ActivityService activityService;

    private Activity testActivity;

    @BeforeEach
    void setUp() {
        testActivity = Activity.builder()
                .id("activity-123")
                .userId("user-123")
                .type(ActivityType.RUNNING)
                .duration(30)
                .caloriesBurned(300)
                .startTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("trackActivity should save and publish to Kafka")
    void trackActivity_ShouldSaveAndPublish() {
        ActivityRequest request = new ActivityRequest();
        request.setUserId("user-123");
        request.setType(ActivityType.RUNNING);
        request.setDuration(30);
        request.setCaloriesBurned(300);
        request.setStartTime(LocalDateTime.now());

        when(userValidationService.validateUser("user-123")).thenReturn(true);
        when(activityRepository.save(any(Activity.class))).thenReturn(testActivity);

        ActivityResponse response = activityService.trackActivity(request);

        assertNotNull(response);
        assertEquals("activity-123", response.getId());
        assertEquals(300, response.getCaloriesBurned());
        verify(kafkaTemplate).send(eq("activity.tracked"), eq("user-123"), any());
    }

    @Test
    @DisplayName("trackActivity should throw for invalid user")
    void trackActivity_ShouldThrowForInvalidUser() {
        ActivityRequest request = new ActivityRequest();
        request.setUserId("invalid-user");

        when(userValidationService.validateUser("invalid-user")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> activityService.trackActivity(request));
        verify(activityRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserActivities should return list")
    void getUserActivities_ShouldReturnList() {
        when(activityRepository.findByUserId("user-123"))
                .thenReturn(List.of(testActivity));

        List<ActivityResponse> activities = activityService.getUserActivities("user-123");

        assertEquals(1, activities.size());
        assertEquals("activity-123", activities.get(0).getId());
    }

    @Test
    @DisplayName("getActivityById should return activity")
    void getActivityById_ShouldReturnActivity() {
        when(activityRepository.findById("activity-123"))
                .thenReturn(Optional.of(testActivity));

        ActivityResponse response = activityService.getActivityById("activity-123");

        assertNotNull(response);
        assertEquals(ActivityType.RUNNING, response.getType());
    }

    @Test
    @DisplayName("getActivityById should throw when not found")
    void getActivityById_ShouldThrowWhenNotFound() {
        when(activityRepository.findById("nonexistent"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> activityService.getActivityById("nonexistent"));
    }
}
