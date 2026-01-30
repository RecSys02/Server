package com.tourai.develop.client.genai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ClovaTextGenerator implements TextGenerator {

    private final WebClient webClient;
    private final String apiKey;

    public ClovaTextGenerator(
            @Value("${clova.api-key}") String apiKey,
            @Value("${clova.base-url:https://clovastudio.stream.ntruss.com}") String baseUrl
    ) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String generate(String model, String instruction, String textInput) {
        String requestId = UUID.randomUUID().toString();

        Map<String, Object> requestBody = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", instruction),
                        Map.of("role", "user", "content", textInput)
                ),
                "topP", 0.8,
                "topK", 0,
                "maxCompletionTokens", 20480,
                "temperature", 0.5,
                "repetitionPenalty", 1.1,
                "includeAiFilters", true,
                "seed", 0,
                "thinking", Map.of("effort", "low")
        );

        Map response = webClient.post()
                .uri("/v3/chat-completions/" + model)
                .header("Authorization", "Bearer " + apiKey)
                .header("X-NCP-CLOVASTUDIO-REQUEST-ID", requestId)
                .header("Accept", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return extractContent(response);
    }

    private String extractContent(Map response) {
        if (response != null && response.get("result") instanceof Map<?, ?> result) {
            if (result.get("message") instanceof Map<?, ?> message) {
                return (String) message.get("content");
            }
        }
        throw new RuntimeException("Failed to generate text from Clova: " + response);
    }
}
