package com.tourai.develop.dto;

import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;

public record SelectedPlaceDto(
        Long placeId,
        Category category,
        Province province

) {}
