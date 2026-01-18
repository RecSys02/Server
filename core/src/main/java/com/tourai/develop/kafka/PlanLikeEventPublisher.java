package com.tourai.develop.kafka;

import com.tourai.develop.kafka.event.PlanLikedEvent;
import com.tourai.develop.kafka.event.PlanLikedInternalEvent;
import com.tourai.develop.kafka.event.PlanUnlikedInternalEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanLikeEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishLike(Long planId, Long userId) {
        applicationEventPublisher.publishEvent(new PlanLikedInternalEvent(planId, userId));
    }

    public void publishUnlike(Long planId, Long userId) {
        applicationEventPublisher.publishEvent(new PlanUnlikedInternalEvent(planId, userId));
    }
}
