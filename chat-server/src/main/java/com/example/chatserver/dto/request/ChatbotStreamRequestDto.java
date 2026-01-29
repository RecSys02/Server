package com.example.chatserver.dto.request;

import com.example.chatserver.dto.ChatMessageDto;

import java.util.List;

public record ChatbotStreamRequestDto(
        String query,
        List<ChatMessageDto> messages,
        List<String> preferredThemes,
        List<String> preferredMoods,
        List<String> preferredRestaurantTypes,
        List<String> preferredCafeTypes,
        List<String> avoid,
        String activityLevel
) {
}
