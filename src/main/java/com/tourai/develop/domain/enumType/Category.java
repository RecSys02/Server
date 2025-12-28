package com.tourai.develop.domain.enumType;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Category {
    CAFE,
    RESTAURANT,
    TOURSPOT;

    @JsonCreator
    public static Category from(String value) {
        if (value == null) return null;

        return switch (value.toLowerCase()) {
            case "tourspot", "tour_spot", "tour-spot" -> TOURSPOT;
            case "cafe" -> CAFE;
            case "restaurant" -> RESTAURANT;
            default -> throw new IllegalArgumentException("Unknown Category: " + value);
        };
    }
}
