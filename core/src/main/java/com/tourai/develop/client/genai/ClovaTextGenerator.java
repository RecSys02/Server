package com.tourai.develop.client.genai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ClovaTextGenerator implements TextGenerator {

    private static final Logger log = LoggerFactory.getLogger(ClovaTextGenerator.class);

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
    public GenAiResponse generate(String model, String instruction, String textInput) {
        String requestId = UUID.randomUUID().toString();

        Map<String, Object> requestBody = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", instruction),
                        Map.of("role", "user", "content", textInput)
                ),
                "topP", 0.8,
                "topK", 0,
                "maxCompletionTokens", 32768,
                "temperature", 0.1,
                "repetitionPenalty", 1.1,
                "stop", List.of(),
                "thinking", Map.of("effort", "none"),
                "responseFormat", getResponseFormat()
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

    private Map<String, Object> getResponseFormat() {
        return Map.of(
                "type", "json",
                "schema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "title", Map.of(
                                        "type", "string",
                                        "description", "여행의 목적지와 테마를 반영하는 매력적이고 직관적인 제목을 설정"
                                ),
                                "schedule", Map.of(
                                        "type", "array",
                                        "description", "일자별 일정 리스트",
                                        "items", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "date", Map.of(
                                                                "type", "string",
                                                                "description", "해당 일정의 날짜 (YYYY-MM-DD)"
                                                        ),
                                                        "activities", Map.of(
                                                                "type", "array",
                                                                "description", "해당 날짜의 여행지 리스트",
                                                                "items", Map.of(
                                                                        "type", "object",
                                                                        "properties", Map.of(
                                                                                "startTime", Map.of("type", "string", "description", "활동 시작 시간 (HH:mm). 이전 장소의 endTime과 최소 30분 간격은 있어야 함"),
                                                                                "endTime", Map.of("type", "string", "description", "활동 종료 시간 (HH:mm). 현재 장소의 startTime과 최소 30분 간격은 있어야 함"),
                                                                                "category", Map.of("type", "string", "description", "장소 카테고리", "enum", List.of("CAFE", "RESTAURANT", "TOURSPOT")),
                                                                                "name", Map.of("type", "string", "description", "장소 이름"),
                                                                                "placeId", Map.of("type", "number", "description", "장소 고유 식별자"),
                                                                                "province", Map.of("type", "string", "description", "지역명", "enum", List.of("SEOUL"))

                                                                        ),
                                                                        "required", List.of("name", "placeId", "category", "province", "startTime", "endTime")
                                                                )
                                                        )
                                                ),
                                                "required", List.of("date", "activities")
                                        )
                                )
                        ),
                        "required", List.of("title", "schedule")
                )
        );
    }

    private GenAiResponse extractContent(Map response) {
        if (response != null && response.get("result") instanceof Map<?, ?> result) {
            if (result.get("message") instanceof Map<?, ?> message) {
                String thinkingContent = (message.get("thinkingContent") instanceof String) ? (String) message.get("thinkingContent") : null;
                if (thinkingContent != null) {
                    log.info("Clova Thinking Content: {}", thinkingContent.replace("\n", " ").replace("\r", " "));
                }
                String content = (String) message.get("content");
                return new GenAiResponse(content, thinkingContent);
            }
        }
        throw new RuntimeException("Failed to generate text from Clova: " + response);
    }
}