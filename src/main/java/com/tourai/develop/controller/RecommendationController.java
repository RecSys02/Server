package com.tourai.develop.controller;

import com.tourai.develop.dto.CustomUserDetails;
import com.tourai.develop.dto.request.RecommendationRequestDto;
import com.tourai.develop.dto.response.AiRecommendationResponse;
import com.tourai.develop.dto.response.RecommendationResponseDto;
import com.tourai.develop.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody RecommendationRequestDto recommendationRequestDto) {
        log.info("RecommendationController.recommend 들어왔음!");
        Long userId = customUserDetails.getUserId();
        log.info("userId : " + userId);
        RecommendationResponseDto recommendationResponseDto = recommendationService.recommend(userId, recommendationRequestDto);
        return ResponseEntity.ok(recommendationResponseDto);
    }
}

