package com.tourai.develop.dto.response;

import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;
import com.tourai.develop.dto.request.RecommendationRequestDto;

import java.util.List;

public record RecommendationResponseDto(
        List<RecommendedPlaceDto> tourspots,
        List<RecommendedPlaceDto> restaurants,
        List<RecommendedPlaceDto> cafes
) {
    public record RecommendedPlaceDto(
            Long id,                 // DB PK
            Long placeId,            // 외부 place_id
            String name,
            Double latitude,
            Double longitude,
            String address,
            String description,
            String duration,
            List<String> images,     // null 가능
            List<String> keywords,   // null 가능
            Category category,
            Province province
    ) {
    }

}
