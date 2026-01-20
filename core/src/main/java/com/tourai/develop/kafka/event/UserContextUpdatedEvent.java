package com.tourai.develop.kafka.event;

import com.tourai.develop.dto.UserContextDto;

public record UserContextUpdatedEvent(
        Long userId,
        UserContextDto userContextDto
) {
    public static UserContextUpdatedEvent of(Long userId, UserContextDto userContextDto) {
        return new UserContextUpdatedEvent(userId, userContextDto);
    }
}
