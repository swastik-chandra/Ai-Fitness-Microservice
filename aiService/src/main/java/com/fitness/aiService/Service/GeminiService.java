package com.fitness.aiService.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GeminiService {

    private final WebClient webClient;

    // ✅ URL ENV / YML se
    @Value("${GEMINI_API_URL}")
    private String geminiUrl;

    // ✅ API KEY ENV se
    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    public GeminiService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    // ✅ ONLY THIS METHOD
    public String getAnswer(String prompt) {

        String requestBody = """
        {
          "contents": [
            {
              "parts": [
                { "text": "%s" }
              ]
            }
          ]
        }
        """.formatted(prompt);

        return webClient.post()
                .uri(geminiUrl + "?key=" + geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
