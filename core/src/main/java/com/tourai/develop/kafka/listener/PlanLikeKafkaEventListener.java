package com.tourai.develop.kafka.listener;

import com.tourai.develop.kafka.event.PlanLikedEvent;
import com.tourai.develop.kafka.event.PlanLikedInternalEvent;
import com.tourai.develop.kafka.event.PlanUnlikedEvent;
import com.tourai.develop.kafka.event.PlanUnlikedInternalEvent;
import com.tourai.develop.kafka.producer.PlanLikeEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class PlanLikeKafkaEventListener {
// publish된 임시 event를 후처리하는 로직

    private final PlanLikeEventProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePlanLiked(PlanLikedInternalEvent event) {
        producer.producePlanLikeEvent(PlanLikedEvent.of(event.planId(), event.userId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePlanUnliked(PlanUnlikedInternalEvent event) {
        producer.producePlanUnlikeEvent(PlanUnlikedEvent.of(event.planId(), event.userId()));
    }
}
