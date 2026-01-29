package com.tourai.develop.dto.response;

import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;
import lombok.Builder;

import java.util.List;

@Builder
public record PlaceResponseDto(
        Long placeId,
        String name,
        Category category,
        Province province,
        String address,
        String duration,
        String description,
        List<String> images,
        List<String> keywords,
        Double latitude,
        Double longitude
) {
    public static PlaceResponseDto from(Place place) {
        return PlaceResponseDto.builder()
                .placeId(place.getPlaceId())
                .name(place.getName())
                .category(place.getCategory())
                .province(place.getProvince())
                .address(place.getAddress())
                .duration(place.getDuration())
                .description(place.getDescription())
                .images(place.getImages())
                .keywords(place.getKeywords())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .build();
    }
}
