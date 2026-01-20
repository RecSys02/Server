package com.tourai.develop.kafka.publisher;

import com.tourai.develop.dto.UserContextDto;
import com.tourai.develop.kafka.event.UserContextUpdatedInternalEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContextEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(Long userId, UserContextDto userContextDto) {
        applicationEventPublisher.publishEvent(new UserContextUpdatedInternalEvent(userId, userContextDto));
    }
}
