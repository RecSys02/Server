package com.tourai.develop.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tourai.develop.dto.PoiDto;
import com.tourai.develop.dto.SelectedPlaceDto;

import java.util.List;

public record RecommendationRequestDto(
        //Ai 추천 최초 실행 시 입력받는 정보
        String region,
        List<String> companion,
        String budget,


        //지금까지 사용자가 선택했던 장소들
        List<SelectedPlaceDto> historyPlaces,

        //사용자가 마지막으로 선택한 장소들
        List<SelectedPlaceDto> selectedPlaces
) {}
