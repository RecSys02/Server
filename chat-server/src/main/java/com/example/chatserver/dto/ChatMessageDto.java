package com.example.chatserver.dto;

public record ChatMessageDto(
        String role,
        String content
) {
}
