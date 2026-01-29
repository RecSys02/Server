package com.example.chatserver.repository;

import com.example.chatserver.dto.UserContextDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserContextRedisRepository {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    @Value("${user-context.redis-key-prefix}")
    private String prefix;
    @Value("${user-context.ttl-seconds}")
    private long ttlSeconds;


    public Mono<UserContextDto> find(Long userId) {
        return redisTemplate.opsForValue()
                .get(key(userId))
                .flatMap(json -> {
                    try {
                        return Mono.just(objectMapper.readValue(json, UserContextDto.class));
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                });
    }

    public Mono<Void> save(Long userId, UserContextDto userContextDto) {
        try {
            String value = objectMapper.writeValueAsString(userContextDto);
            return redisTemplate.opsForValue()
                    .set(key(userId), value, java.time.Duration.ofSeconds(ttlSeconds))
                    .then();
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private String key(Long userId) {
        return prefix + userId;
    }

}
