package com.example.chatserver.dto.request;

import com.example.chatserver.dto.AiSelectedPlaceDto;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiRecommendationRequest(
        Long userId,
        List<String> preferredThemes,
        List<String> preferredMoods,
        List<String> preferredRestaurantTypes,
        List<String> preferredCafeTypes,
        List<String> avoid,
        String activityLevel,
        String region,
        String accomAddress,
        List<String> companion,
        String budget,
        List<AiSelectedPlaceDto> historyPlaces,
        List<AiSelectedPlaceDto> selectedPlaces
) {
}
