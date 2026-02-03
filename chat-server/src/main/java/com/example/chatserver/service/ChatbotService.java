package com.example.chatserver.service;

import com.example.chatserver.dto.ChatMessageDto;
import com.example.chatserver.dto.UserContextDto;
import com.example.chatserver.dto.request.ChatbotRequest;
import com.example.chatserver.dto.request.ChatbotStreamRequestDto;
import com.example.chatserver.repository.ChatContextRedisRepository;
import com.example.chatserver.repository.ChatHistoryRedisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final WebClient chatbotWebClient;
    private final UserContextService userContextService;
    private final ChatHistoryRedisRepository chatHistoryRedisRepository;
    private final ChatContextRedisRepository chatContextRedisRepository;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public Flux<ServerSentEvent<String>> getChatStream(ChatbotRequest request) {
        return authService.currentUserId()
                .flatMapMany(userId -> Mono.zip(
                        userContextService.getOrFetch(userId),
                        chatHistoryRedisRepository.getChatHistory(userId),
                        chatContextRedisRepository.getChatContext(userId).defaultIfEmpty("")
                ).flatMapMany(tuple -> {
                    UserContextDto userContext = tuple.getT1();
                    List<ChatMessageDto> history = tuple.getT2();
                    String chatContext = tuple.getT3();

                    // 사용자 질문을 히스토리에 추가
                    ChatMessageDto userMessage = new ChatMessageDto("user", request.query());
                    history.add(userMessage);

                    // Redis에 값이 없어서 빈 문자열로 넘어온 경우 null로 변환
                    // 값이 있다면 JSON 파싱 시도
                    Object contextToSend = null;
                    if (!"".equals(chatContext)) {
                        try {
                            contextToSend = objectMapper.readValue(chatContext, Object.class);
                        } catch (JsonProcessingException e) {
                            log.warn("Failed to parse chat context as JSON. userId: {}", userId);
                            contextToSend = chatContext;
                        }
                    }

                    // AI 서버로 보낼 요청 DTO 생성
                    ChatbotStreamRequestDto streamRequest = new ChatbotStreamRequestDto(
                            request.query(),
                            history,
                            contextToSend,
                            userContext.preferredThemes(),
                            userContext.preferredMoods(),
                            userContext.preferredRestaurantTypes(),
                            userContext.preferredCafeTypes(),
                            userContext.avoid(),
                            userContext.activityLevel()
                    );

                    // 1. 사용자 질문이 포함된 히스토리를 먼저 저장 (데이터 유실 방지)
                    return chatHistoryRedisRepository.saveChatHistory(userId, history)
                            .thenMany(chatbotWebClient.post()
                                    .uri("/chat/stream")
                                    .bodyValue(streamRequest)
                                    .retrieve()
                                    .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                                    .doOnNext(event -> {
                                        if ("final".equals(event.event()) && event.data() != null) {
                                            // 2. 완성된 응답을 히스토리에 추가 및 저장
                                            // 주의: subscribe()는 Fire-and-Forget 방식입니다.
                                            // 운영 환경에서는 에러 핸들링이나 Schedulers 설정이 필요할 수 있습니다.
                                            ChatMessageDto aiMessage = new ChatMessageDto("assistant", event.data());
                                            history.add(aiMessage);
                                            chatHistoryRedisRepository.saveChatHistory(userId, history).subscribe();
                                        } else if ("context".equals(event.event()) && event.data() != null) {
                                            // 3. context 이벤트가 오면 Redis에 저장
                                            chatContextRedisRepository.saveChatContext(userId, event.data()).subscribe();
                                        }
                                    })
                                    .onErrorResume(e -> Flux.just(
                                            ServerSentEvent.<String>builder()
                                                    .event("error")
                                                    .data("서버 통신 중 오류 발생: " + e.getMessage())
                                                    .build()
                                    )));
                }));
    }

    public Mono<List<ChatMessageDto>> getChatHistory() {
        return authService.currentUserId()
                .flatMap(chatHistoryRedisRepository::getChatHistory);
    }
}
