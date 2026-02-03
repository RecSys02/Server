package com.example.chatserver.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiRecommendationResponse(
        List<CategoryAndRecommendedItems> recommendations
) {
    public record CategoryAndRecommendedItems(
            String category,
            List<RecommendedItem> items
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record RecommendedItem(
            String category,
            String province,
            Long placeId,
            Double score
    ) {
    }
}
