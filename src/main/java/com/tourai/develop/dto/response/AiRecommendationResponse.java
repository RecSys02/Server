package com.tourai.develop.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiRecommendationResponse(
        List<CategoryAndRecommendedItems> recommendations

) {

    public record CategoryAndRecommendedItems(
       String category,
       List<RecommendedItem>items
    ){}


    public record RecommendedItem(
       String region,
       String category,
       @JsonProperty("place_id")
       Long placeId,
       Double score

    ){}
}
