package com.tourai.develop.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tourai.develop.kafka.enumType.KafkaTopic;
import com.tourai.develop.kafka.event.UserContextUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserContextEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public void produceEvent(UserContextUpdatedEvent event) {
        produce(KafkaTopic.USER_CONTEXT_UPDATED.getTopic(), event.userId().toString(), event);
    }

    private void produce(String topic, String key, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, payload);
            log.info("[UserContextEventProducer] produced. topic = {}, key = {}", topic, key);
        } catch (Exception e) {
            log.info("[UserContextEventProducer] produce failed.. topic = {}, key = {}", topic, key);
            throw new RuntimeException(e);
        }
    }

}
