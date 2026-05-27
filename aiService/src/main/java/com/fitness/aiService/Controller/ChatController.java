package com.fitness.aiService.Controller;

import com.fitness.aiService.Service.ChatbotService;
import com.fitness.aiService.model.ChatMessage;
import com.fitness.aiService.model.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String message = request.get("message");
        String sessionId = request.getOrDefault("sessionId", null);

        ChatMessage response = chatbotService.chat(userId, message, sessionId);
        return ResponseEntity.ok(Map.of(
                "message", response.getContent(),
                "timestamp", response.getTimestamp().toString()
        ));
    }

    @GetMapping("/sessions/{userId}")
    public ResponseEntity<List<ChatSession>> getSessions(@PathVariable String userId) {
        return ResponseEntity.ok(chatbotService.getUserSessions(userId));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ChatSession> getSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatbotService.getSession(sessionId));
    }
}
