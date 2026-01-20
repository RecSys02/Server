package com.example.chatserver.service;

import com.example.chatserver.client.UserContextClient;
import com.example.chatserver.dto.UserContextDto;
import com.example.chatserver.repository.UserContextRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UserContextRedisRepository redisRepository;
    private final UserContextClient client;

    public Mono<UserContextDto> getOrFetch(Long userId) {
        return redisRepository.find(userId)
                .switchIfEmpty(
                        client.fetch(userId)
                                .flatMap(contextDto -> redisRepository.save(userId, contextDto).thenReturn(contextDto))
                );
    }
}
