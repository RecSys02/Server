package com.example.chatserver.controller;

import com.example.chatserver.dto.UserContextDto;
import com.example.chatserver.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/debug")
public class UserContextController {
    private final UserContextService userContextService;

    @GetMapping("/user-context/{userId}")
    public Mono<UserContextDto> get(@PathVariable("userId") Long userId) {
        // redis2에 해당 user context 정보가 존재할 시 읽어오고, 존재하지 않을 시 core 서버에 api 요청 보내서 redis2에 저장
        return userContextService.getOrFetch(userId);
    }
}
