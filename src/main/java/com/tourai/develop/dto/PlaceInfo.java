package com.tourai.develop.dto;

import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Region;
import java.util.List;

public record PlaceInfo(
    Long placeId,
    Category category,
    Region region,
    String name,
    String address,
    String duration,
    String description,
    List<String> images,
    Double latitude,
    Double longitude
) {}
