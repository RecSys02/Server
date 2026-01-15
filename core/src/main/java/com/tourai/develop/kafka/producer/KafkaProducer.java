package com.tourai.develop.kafka.producer;

import com.tourai.develop.kafka.enumType.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(KafkaTopic topic, String key, String message) {
        kafkaTemplate.send(topic.getTopic(), key, message)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.info("Kafka 전송 실패. topic = {}, key = {}, message = {}",
                                topic.getTopic(), key, message);
                    } else {
                        log.info("Kafka 전송 성공! topic = {}, key = {}, message = {}, partition= {}, partition_offset = {}",
                                topic.getTopic(), key, message,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
