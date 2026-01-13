package com.tourai.develop.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class TempKafkaServiceTest {

    @Autowired
    TempKafkaService tempKafkaService;

    @Test
    void send() throws InterruptedException {
        tempKafkaService.send("please..3");
    }
}