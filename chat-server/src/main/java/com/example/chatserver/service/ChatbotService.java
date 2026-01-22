package com.example.chatserver.service;

import com.example.chatserver.dto.ChatMessageDto;
import com.example.chatserver.dto.UserContextDto;
import com.example.chatserver.dto.request.ChatbotRequest;
import com.example.chatserver.dto.request.ChatbotStreamRequestDto;
import com.example.chatserver.repository.ChatHistoryRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final WebClient chatbotWebClient;
    private final UserContextService userContextService;
    private final ChatHistoryRedisRepository chatHistoryRedisRepository;
    private final AuthService authService;

    public Flux<ServerSentEvent<String>> getChatStream(ChatbotRequest request) {
        return authService.currentUserId()
                .flatMapMany(userId -> Mono.zip(
                        userContextService.getOrFetch(userId),
                        chatHistoryRedisRepository.getChatHistory(userId)
                ).flatMapMany(tuple -> {
                    UserContextDto userContext = tuple.getT1();
                    List<ChatMessageDto> history = tuple.getT2();

                    // 사용자 질문을 히스토리에 추가
                    ChatMessageDto userMessage = new ChatMessageDto("user", request.query());
                    history.add(userMessage);

                    // AI 서버로 보낼 요청 DTO 생성
                    ChatbotStreamRequestDto streamRequest = new ChatbotStreamRequestDto(
                            request.query(),
                            history,
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
}