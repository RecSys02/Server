package com.tourai.develop.controller;

import com.tourai.develop.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;
}

