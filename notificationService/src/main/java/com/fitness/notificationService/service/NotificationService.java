package com.fitness.notificationService.service;

import com.fitness.notificationService.model.Notification;
import com.fitness.notificationService.model.NotificationType;
import com.fitness.notificationService.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    public Notification createAndSend(String userId, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = repository.save(notification);
        log.info("Created notification for user {}: {}", userId, title);

        // Push to WebSocket
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, saved);
        return saved;
    }

    public List<Notification> getUserNotifications(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(String userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    public Notification markAsRead(String notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return repository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> unread = repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
        log.info("Marked {} notifications as read for user {}", unread.size(), userId);
    }
}
