package com.example.chatserver.controller;

import com.example.chatserver.dto.ChatMessageDto;
import com.example.chatserver.dto.request.ChatbotRequest;
import com.example.chatserver.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatbotStream(@RequestBody ChatbotRequest request) {
        // 서비스로 받은 Flux 그대로 반환
        return chatbotService.getChatStream(request);
    }

    @GetMapping("/history")
    public Mono<List<ChatMessageDto>> getChatHistory() {
        return chatbotService.getChatHistory();
    }
}
