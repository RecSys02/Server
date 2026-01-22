package com.example.chatserver.service;

import com.example.chatserver.dto.ChatMessageDto;
import com.example.chatserver.dto.UserContextDto;
import com.example.chatserver.dto.request.ChatbotRequest;
import com.example.chatserver.repository.ChatHistoryRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private WebClient chatbotWebClient;
    @Mock
    private UserContextService userContextService;
    @Mock
    private ChatHistoryRedisRepository chatHistoryRedisRepository;
    @Mock
    private AuthService authService;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService(chatbotWebClient, userContextService, chatHistoryRedisRepository, authService);
    }

    @Test
    void getChatStream_ShouldReturnMockedStream() {
        // Given
        Long userId = 1L;
        ChatbotRequest request = new ChatbotRequest("안녕");
        UserContextDto userContext = new UserContextDto(
                List.of("theme"), List.of("mood"), List.of("restaurant"),
                List.of("cafe"), List.of("avoid"), "active"
        );
        List<ChatMessageDto> history = new ArrayList<>();

        when(authService.currentUserId()).thenReturn(Mono.just(userId));
        when(userContextService.getOrFetch(userId)).thenReturn(Mono.just(userContext));
        when(chatHistoryRedisRepository.getChatHistory(userId)).thenReturn(Mono.just(history));
        when(chatHistoryRedisRepository.saveChatHistory(anyLong(), any())).thenReturn(Mono.empty());

        // WebClient Mocking
        when(chatbotWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        Flux<ServerSentEvent<String>> mockResponse = Flux.just(
                ServerSentEvent.<String>builder().event("token").data("안녕").build(),
                ServerSentEvent.<String>builder().event("token").data("하세요").build(),
                ServerSentEvent.<String>builder().event("final").data("안녕하세요").build()
        );
        
        when(responseSpec.bodyToFlux(any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(mockResponse);

        // When & Then
        StepVerifier.create(chatbotService.getChatStream(request))
                .expectNextMatches(event -> "token".equals(event.event()) && "안녕".equals(event.data()))
                .expectNextMatches(event -> "token".equals(event.event()) && "하세요".equals(event.data()))
                .expectNextMatches(event -> "final".equals(event.event()) && "안녕하세요".equals(event.data()))
                .verifyComplete();
    }
}
