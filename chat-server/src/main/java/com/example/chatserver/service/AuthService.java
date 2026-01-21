package com.example.chatserver.service;

import com.example.chatserver.filter.JwtUserContextFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AuthService {


    public Mono<Long> currentUserId() {
        return Mono.deferContextual(contextView -> {
            if (!contextView.hasKey(JwtUserContextFilter.CTX_USER_ID)) {
                return Mono.error(new RuntimeException("Unauthorized: userId missing"));
            }
            Long userId = contextView.get(JwtUserContextFilter.CTX_USER_ID);
            log.info("AuthService 에서 userId = {} 정상추출!", userId);
            return Mono.just(userId);
        });
    }


}
