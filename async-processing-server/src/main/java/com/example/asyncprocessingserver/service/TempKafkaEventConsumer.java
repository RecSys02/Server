package com.example.asyncprocessingserver.service;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
public class TempKafkaEventConsumer {

    @KafkaListener(topics = "temp", groupId = "temp-service")
    @RetryableTopic(attempts = "5",
            backoff = @Backoff(delay = 1000, multiplier = 2),
            dltTopicSuffix = ".dlt")
    public void read(String message) {
        System.out.println("message : " + message);
    }
}
