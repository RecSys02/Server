package com.example.chatserver.dto;

import java.util.List;

public record PlaceResponseDto(
        Long placeId,
        String name,
        String category,
        String province,
        String address,
        String duration,
        String description,
        List<String> images,
        List<String> keywords,
        Double latitude,
        Double longitude
) {
}
