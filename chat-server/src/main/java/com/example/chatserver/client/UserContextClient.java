package com.example.chatserver.client;

import com.example.chatserver.dto.UserContextDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserContextClient {

    private final WebClient coreWebClient;

    public Mono<UserContextDto> fetch(Long userId) {
        return coreWebClient.get()
                .uri("/internal/users/context/{userId}", userId)
                .retrieve()
                .bodyToMono(UserContextDto.class);
    }

}
