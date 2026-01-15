package com.tourai.develop.kafka.config;

import com.tourai.develop.kafka.enumType.KafkaTopic;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic tempTopic() {
        return TopicBuilder.name(KafkaTopic.TEMP.getTopic())
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic userSignedUpTopic() {
        return TopicBuilder.name(KafkaTopic.USER_SIGNED_UP.getTopic())
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic emailSendTopic() {
        return TopicBuilder.name(KafkaTopic.EMAIL_SEND.getTopic())
                .partitions(3)
                .replicas(3)
                .build();
    }
}
