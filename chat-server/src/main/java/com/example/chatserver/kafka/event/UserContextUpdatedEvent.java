package com.example.chatserver.kafka.event;

import com.example.chatserver.dto.UserContextDto;

public record UserContextUpdatedEvent(
        Long userId,
        UserContextDto userContextDto
) {

}
