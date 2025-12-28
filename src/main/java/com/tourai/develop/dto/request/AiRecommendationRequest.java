package com.tourai.develop.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tourai.develop.dto.AiSelectedPlaceDto;
import com.tourai.develop.dto.SelectedPlaceDto;

import java.util.List;
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiRecommendationRequest(

        // 사용자 id 값으로 db에서 조회해서 채울 정보들
        Long userId,
        List<String> preferredThemes,
        List<String> preferredMoods,
        List<String> preferredRestaurantTypes,
        List<String> preferredCafeTypes,
        List<String> avoid,
        String activityLevel,


        //Ai 추천 최초 실행 시 입력받는 정보
        String region,
        List<String> companion,
        String budget,


        //지금까지 사용자가 선택했던 장소들
        List<AiSelectedPlaceDto> historyPlaces,

        //사용자가 마지막으로 선택한 장소들
        List<AiSelectedPlaceDto> selectedPlaces


) {
}
