package com.fitness.aiService.Service;

import com.fitness.aiService.Repository.ChatSessionRepository;
import com.fitness.aiService.model.ChatMessage;
import com.fitness.aiService.model.ChatSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {

    private final GeminiService geminiService;
    private final ChatSessionRepository chatSessionRepository;

    private static final String SYSTEM_PROMPT = """
            You are FitAI Coach, an expert AI fitness coach. You provide personalized advice on:
            - Workout routines and exercise form
            - Recovery and injury prevention
            - Fitness goals and motivation
            - General health and wellness
            
            Be encouraging, knowledgeable, and concise. Use emojis occasionally.
            If asked about medical conditions, recommend consulting a healthcare professional.
            Keep responses under 300 words unless detailed explanation is needed.
            """;

    public ChatMessage chat(String userId, String message, String sessionId) {
        ChatSession session;

        if (sessionId != null && !sessionId.isEmpty()) {
            session = chatSessionRepository.findById(sessionId)
                    .orElse(createNewSession(userId));
        } else {
            session = createNewSession(userId);
        }

        // Add user message
        ChatMessage userMsg = ChatMessage.builder()
                .role("user")
                .content(message)
                .timestamp(LocalDateTime.now())
                .build();
        session.getMessages().add(userMsg);

        // Build context from conversation history
        String context = buildContext(session.getMessages());

        // Get AI response
        String aiResponse;
        try {
            aiResponse = geminiService.getAnswer(context);
            // Parse the response to extract just the text
            aiResponse = extractTextFromGeminiResponse(aiResponse);
        } catch (Exception e) {
            log.error("AI chat failed: ", e);
            aiResponse = "I'm having trouble thinking right now. Please try again in a moment! 🤔";
        }

        // Add assistant message
        ChatMessage assistantMsg = ChatMessage.builder()
                .role("assistant")
                .content(aiResponse)
                .timestamp(LocalDateTime.now())
                .build();
        session.getMessages().add(assistantMsg);
        session.setUpdatedAt(LocalDateTime.now());

        // Keep only last 20 messages per session
        if (session.getMessages().size() > 20) {
            session.setMessages(session.getMessages().subList(
                    session.getMessages().size() - 20, session.getMessages().size()));
        }

        chatSessionRepository.save(session);
        return assistantMsg;
    }

    public List<ChatSession> getUserSessions(String userId) {
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public ChatSession getSession(String sessionId) {
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found"));
    }

    private ChatSession createNewSession(String userId) {
        return ChatSession.builder()
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private String buildContext(List<ChatMessage> messages) {
        String history = messages.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        return SYSTEM_PROMPT + "\n\nConversation:\n" + history + "\nassistant:";
    }

    private String extractTextFromGeminiResponse(String response) {
        // Simple extraction — in production use proper JSON parsing
        if (response.contains("\"text\"")) {
            int start = response.indexOf("\"text\"") + 9;
            int end = response.indexOf("\"", start);
            if (end > start) {
                return response.substring(start, end)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"");
            }
        }
        return response.length() > 500 ? response.substring(0, 500) : response;
    }
}
