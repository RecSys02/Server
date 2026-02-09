package com.example.chatserver.dto.request;

import com.example.chatserver.dto.SelectedPlaceDto;

import java.util.List;

public record RecommendationRequestDto(
        String region,
        String accomAddress,
        List<String> companion,
        String budget,
        List<SelectedPlaceDto> historyPlaces,
        List<SelectedPlaceDto> selectedPlaces
) {
}
