package com.tourai.develop.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record RecommendationDto(


        //Ai 추천 최초 실행 시 입력받는 정보
        String city,
        List<String> companionType,
        String budget,


        //지금까지 사용자가 선택했던 장소들
        List<PoiDto> visitCafe,
        List<PoiDto> visitRestaurant,
        List<PoiDto> visitTourspot,


        //사용자가 마지막으로 선택한 장소들
        List<PoiDto> lastSelectedPoi

) {
}
