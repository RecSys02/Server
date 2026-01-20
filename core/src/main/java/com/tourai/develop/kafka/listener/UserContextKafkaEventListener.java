package com.tourai.develop.kafka.listener;

import com.tourai.develop.kafka.event.UserContextUpdatedEvent;
import com.tourai.develop.kafka.event.UserContextUpdatedInternalEvent;
import com.tourai.develop.kafka.producer.UserContextEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserContextKafkaEventListener {
    private final UserContextEventProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserContextUpdatedInternalEvent event) {
        producer.produceEvent(UserContextUpdatedEvent.of(event.userId(), event.userContextDto()));
    }
}
