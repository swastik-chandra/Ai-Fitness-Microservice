package com.fitness.aiService.Repository;

import com.fitness.aiService.model.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(String userId);
    Optional<ChatSession> findFirstByUserIdOrderByUpdatedAtDesc(String userId);
}
