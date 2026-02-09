package com.example.chatserver.dto;

public record SelectedPlaceDto(
        Long placeId,
        String category,
        String province
) {}
