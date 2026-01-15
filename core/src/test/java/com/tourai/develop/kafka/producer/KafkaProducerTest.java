package com.tourai.develop.kafka.producer;

import com.tourai.develop.kafka.enumType.KafkaTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KafkaProducerTest {

    @Autowired
    KafkaProducer kafkaProducer;

    @Test
    void send() throws InterruptedException {
        kafkaProducer.send(KafkaTopic.TEMP, "user-1", "hello3");
        kafkaProducer.send(KafkaTopic.TEMP, "user-1", "hello4");
        Thread.sleep(5000);
    }

    @Test
    void sendRoundRobin() throws InterruptedException {
        //멀티스레드(3개 스레드가 3개의 파티션을 동시에 병렬처리) + 라운드 로빈 방식 정상작동
        kafkaProducer.send(KafkaTopic.TEMP, null, "rr1");
        kafkaProducer.send(KafkaTopic.TEMP, null, "rr2");
        kafkaProducer.send(KafkaTopic.TEMP, null, "rr3");
        Thread.sleep(5000);
    }
}