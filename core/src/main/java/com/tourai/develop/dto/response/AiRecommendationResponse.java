package com.tourai.develop.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiRecommendationResponse(
        List<CategoryAndRecommendedItems> recommendations

) {

    public record CategoryAndRecommendedItems(
            Category category,
            List<RecommendedItem> items
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record RecommendedItem(
            Category category,
            Province province,
            Long placeId,
            Double score

    ) {
    }
}
