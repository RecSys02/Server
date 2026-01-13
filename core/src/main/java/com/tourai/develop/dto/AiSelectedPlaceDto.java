package com.tourai.develop.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiSelectedPlaceDto(
        Long placeId,
        Category category,
        Province province

) {}
