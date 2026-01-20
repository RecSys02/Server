package com.tourai.develop.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public record EditUserTagsDto(
        List<String> preferredThemes,
        List<String> preferredMoods,
        List<String> preferredRestaurantTypes,
        List<String> preferredCafeTypes,
        List<String> avoid,
        String activityLevel
) {

}
