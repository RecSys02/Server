package com.example.chatserver.controller;

import com.example.chatserver.dto.request.RecommendationRequestDto;
import com.example.chatserver.dto.response.RecommendationResponseDto;
import com.example.chatserver.filter.JwtUserContextFilter;
import com.example.chatserver.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
@Tag(name = "Recommendation", description = "AI 추천 관련 API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Operation(summary = "AI 여행지 추천", description = "사용자의 취향과 요청 정보를 바탕으로 여행지, 맛집, 카페를 추천합니다.")
    public Mono<RecommendationResponseDto> recommend(
            @RequestBody RecommendationRequestDto requestDto) {
        
        return Mono.deferContextual(ctx -> {
            Long userId = ctx.get(JwtUserContextFilter.CTX_USER_ID);
            return recommendationService.recommend(userId, requestDto);
        });
    }
}
