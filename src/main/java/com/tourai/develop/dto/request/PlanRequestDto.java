package com.tourai.develop.dto.request;

import com.tourai.develop.domain.enumType.Region;
import lombok.Builder;

import java.util.List;

@Builder
public record PlanRequestDto(
    Long userId,
    List<Long> placeIds,
    List<Long> tagIds,
    String name,
    Integer duration,
    Region placeRegion,
    Boolean isPrivate
) { }
