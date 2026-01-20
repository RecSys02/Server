package com.tourai.develop.kafka.event;

import com.tourai.develop.dto.UserContextDto;

public record UserContextUpdatedInternalEvent(
        Long userId,
        UserContextDto userContextDto

) {
}
