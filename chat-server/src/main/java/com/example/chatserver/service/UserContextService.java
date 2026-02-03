package com.example.chatserver.service;

import com.example.chatserver.client.CoreClient;
import com.example.chatserver.dto.UserContextDto;
import com.example.chatserver.repository.UserContextRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UserContextRedisRepository redisRepository;
    private final CoreClient client;

    public Mono<UserContextDto> getOrFetch(Long userId) {
        // 레디스에서 읽기 시도, 없으면 core 서버로 api 요청하는 방식
        return redisRepository.find(userId)
                .switchIfEmpty(
                        client.fetchUserContext(userId)
                                .flatMap(contextDto -> redisRepository.save(userId, contextDto).thenReturn(contextDto))
                );
    }
    public Mono<UserContextDto> fetch(Long userId) {
        //항상 core 서버로 api 요청하는 방식
        return client.fetchUserContext(userId);
    }

}
