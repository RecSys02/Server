package com.example.chatserver.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ChatContextRedisRepository {
    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${chat-context.redis-key-prefix:chat:context:}")
    private String prefix;

    @Value("${chat-context.ttl-seconds:3600}")
    private long ttlSeconds;

    public Mono<String> getChatContext(Long userId) {
        return redisTemplate.opsForValue()
                .get(key(userId));
    }

    public Mono<Void> saveChatContext(Long userId, String context) {
        return redisTemplate.opsForValue()
                .set(key(userId), context, Duration.ofSeconds(ttlSeconds))
                .then();
    }

    private String key(Long userId) {
        return prefix + userId;
    }
}
