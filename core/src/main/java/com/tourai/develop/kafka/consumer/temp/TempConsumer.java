package com.tourai.develop.kafka.consumer.temp;
import com.tourai.develop.kafka.enumType.KafkaConsumerGroup;
import com.tourai.develop.kafka.enumType.KafkaTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TempConsumer {
    @KafkaListener(
            topics = "#{T(com.tourai.develop.kafka.enumType.KafkaTopic).TEMP.getTopic()}",
            groupId = "#{T(com.tourai.develop.kafka.enumType.KafkaConsumerGroup).TEMP_SERVICE.getGroupId()}",
            concurrency = "3"
    )
    public void consume(String message) {
        log.info("[TEMP] message = {}", message);
    }
}
