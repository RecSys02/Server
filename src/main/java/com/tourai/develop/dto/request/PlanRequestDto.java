package com.tourai.develop.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record PlanRequestDto(
    Long userId,
    List<Long> placeIds,
    List<Long> tagIds,
    String name,
    Boolean isPrivate
) { }
