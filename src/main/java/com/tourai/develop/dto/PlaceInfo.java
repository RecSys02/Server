package com.tourai.develop.dto;

import com.tourai.develop.domain.enumType.Region;
import java.util.List;

public record PlaceInfo(
    Long placeId,
    Region region,
    String name,
    String description,
    List<String> pictures,
    Double latitude,
    Double longitude
) {}
