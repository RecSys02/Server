package com.example.chatserver.kafka.consumer;

import com.example.chatserver.dto.UserContextDto;
import com.example.chatserver.kafka.event.UserContextUpdatedEvent;
import com.example.chatserver.repository.UserContextRedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserContextUpdatedConsumer {

    private final ObjectMapper objectMapper;
    private final UserContextRedisRepository redisRepository;

    @KafkaListener(
            topics = "user.context.updated",
            groupId = "chat-server"
    )
    public void consume(String message) {
        try {
            UserContextUpdatedEvent event = objectMapper.readValue(message, UserContextUpdatedEvent.class);

            Long userId = event.userId();
            UserContextDto userContextDto = event.userContextDto();

            if (userId == null || userContextDto == null) {
                log.info("[UserContextUpdateConsumer.consume] userId 또는 userContextDto 가 null 입니다.");
                return;
            }

            redisRepository.save(userId, userContextDto)
                    .doOnSuccess(v -> log.info("[UserContextUpdateConsumer.consume] userContextDto successfully synchronized, userId={}", userId))
                    .doOnError(e -> log.info("[UserContextUpdateConsumer.consume] userContextDto synchronized failed.., userId={}", userId))
                    .subscribe();

        } catch (Exception e) {
            log.info("[UserContextUpdatedConsumer.consume] parse/handle error, message={}", message, e);
        }
    }
}
