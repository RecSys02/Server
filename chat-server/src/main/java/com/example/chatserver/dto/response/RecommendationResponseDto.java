package com.example.chatserver.dto.response;

import com.example.chatserver.dto.PlaceResponseDto;

import java.util.List;

public record RecommendationResponseDto(
        List<PlaceResponseDto> tourspots,
        List<PlaceResponseDto> restaurants,
        List<PlaceResponseDto> cafes
) {
}
