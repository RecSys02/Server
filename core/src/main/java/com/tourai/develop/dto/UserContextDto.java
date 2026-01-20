package com.tourai.develop.dto;
import java.util.List;
public record UserContextDto(
        List<String> preferredThemes,
        List<String> preferredMoods,
        List<String> preferredRestaurantTypes,
        List<String> preferredCafeTypes,
        List<String> avoid,
        String activityLevel

) {

}
