package com.tourai.develop.kafka.event;

public record PlanUnlikedInternalEvent(
        Long planId,
        Long userId
) {
}
