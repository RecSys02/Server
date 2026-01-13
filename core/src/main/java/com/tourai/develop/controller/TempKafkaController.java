package com.tourai.develop.controller;

import com.tourai.develop.service.TempKafkaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TempKafkaController {
    private final TempKafkaService tempKafkaService;

    @PostMapping("/kafka/send/{message}")
    public ResponseEntity<String> send(
            @PathVariable("message") String message
    ) {
        tempKafkaService.send(message);
        return ResponseEntity.ok(message);
    }

}
