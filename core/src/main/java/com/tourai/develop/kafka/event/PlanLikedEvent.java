package com.tourai.develop.kafka.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlanLikedEvent(
        String eventId,
        Long planId,
        Long userId,
        OffsetDateTime occurredAt
) {
    public static PlanLikedEvent of(Long planId, Long userId) {
        return new PlanLikedEvent(UUID.randomUUID().toString(),
                planId,
                userId,
                OffsetDateTime.now());
    }
}
