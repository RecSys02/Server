package com.tourai.develop.kafka.event;

public record PlanLikedInternalEvent(
        Long planId,
        Long userId
) {
}
