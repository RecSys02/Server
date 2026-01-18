package com.tourai.develop.kafka.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlanUnlikedEvent(
        String eventId,
        Long planId,
        Long userId,
        OffsetDateTime occurredAt
) {
    public static PlanUnlikedEvent of(Long planId, Long userId) {
        return new PlanUnlikedEvent(UUID.randomUUID().toString(),
                planId,
                userId,
                OffsetDateTime.now());
    }
}
