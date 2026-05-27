package com.fitness.notificationService.service;

import com.fitness.notificationService.model.Notification;
import com.fitness.notificationService.model.NotificationType;
import com.fitness.notificationService.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("createAndSend should save and push via WebSocket")
    void createAndSend_ShouldSaveAndPush() {
        Notification saved = Notification.builder()
                .id("notif-1")
                .userId("user-1")
                .type(NotificationType.WELCOME)
                .title("Welcome!")
                .message("Hello")
                .build();

        when(repository.save(any(Notification.class))).thenReturn(saved);

        Notification result = notificationService.createAndSend(
                "user-1", NotificationType.WELCOME, "Welcome!", "Hello");

        assertNotNull(result);
        assertEquals("notif-1", result.getId());
        verify(messagingTemplate).convertAndSend(
                eq("/topic/notifications/user-1"), any(Notification.class));
    }

    @Test
    @DisplayName("getUnreadCount should return correct count")
    void getUnreadCount_ShouldReturnCount() {
        when(repository.countByUserIdAndReadFalse("user-1")).thenReturn(5L);

        long count = notificationService.getUnreadCount("user-1");

        assertEquals(5, count);
    }

    @Test
    @DisplayName("markAsRead should update notification")
    void markAsRead_ShouldUpdateNotification() {
        Notification notif = Notification.builder()
                .id("notif-1")
                .read(false)
                .build();

        when(repository.findById("notif-1")).thenReturn(Optional.of(notif));
        when(repository.save(any())).thenReturn(notif);

        Notification result = notificationService.markAsRead("notif-1");

        assertTrue(result.isRead());
        verify(repository).save(notif);
    }

    @Test
    @DisplayName("markAllAsRead should update all unread")
    void markAllAsRead_ShouldUpdateAll() {
        List<Notification> unread = List.of(
                Notification.builder().id("1").read(false).build(),
                Notification.builder().id("2").read(false).build()
        );

        when(repository.findByUserIdAndReadFalseOrderByCreatedAtDesc("user-1"))
                .thenReturn(unread);

        notificationService.markAllAsRead("user-1");

        verify(repository).saveAll(unread);
    }
}
