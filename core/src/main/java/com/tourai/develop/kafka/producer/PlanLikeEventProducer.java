package com.tourai.develop.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tourai.develop.kafka.enumType.KafkaTopic;
import com.tourai.develop.kafka.event.PlanLikedEvent;
import com.tourai.develop.kafka.event.PlanUnlikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlanLikeEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    public void producePlanLikeEvent(PlanLikedEvent event) {
        produce(KafkaTopic.PLAN_LIKED.getTopic(), event.planId().toString(), event);
    }

    public void producePlanUnlikeEvent(PlanUnlikedEvent event) {
        produce(KafkaTopic.PLAN_UNLIKED.getTopic(), event.planId().toString(), event);
    }

    private void produce(String topic, String key, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, payload);
            log.info("[PlanLikeEventProducer] produced. topic = {}, key = {}", topic, key);
        } catch (Exception e) {
            // 여기서 실패하면 outbox 패턴이 이상적이지만, MVP는 일단 로그 + 예외 처리로 시작
            log.info("[PlanLikeEventProducer] produce failed.. topic = {}, key = {}", topic, key);
            throw new RuntimeException(e);
        }
    }

}
