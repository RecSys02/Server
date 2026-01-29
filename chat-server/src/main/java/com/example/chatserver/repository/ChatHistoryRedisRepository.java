package com.example.chatserver.repository;

import com.example.chatserver.dto.ChatMessageDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatHistoryRedisRepository {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chat-history.redis-key-prefix:chat:history:}")
    private String prefix;

    @Value("${chat-history.ttl-seconds:3600}")
    private long ttlSeconds;

    public Mono<List<ChatMessageDto>> getChatHistory(Long userId) {
        return redisTemplate.opsForValue()
                .get(key(userId))
                .flatMap(json -> {
                    try {
                        List<ChatMessageDto> list = objectMapper.readValue(json, new TypeReference<List<ChatMessageDto>>() {});
                        return Mono.just(list);
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                })
                .defaultIfEmpty(new ArrayList<>());
    }

    public Mono<Void> saveChatHistory(Long userId, List<ChatMessageDto> messages) {
        try {
            String value = objectMapper.writeValueAsString(messages);
            return redisTemplate.opsForValue()
                    .set(key(userId), value, Duration.ofSeconds(ttlSeconds))
                    .then();
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private String key(Long userId) {
        return prefix + userId;
    }
}
