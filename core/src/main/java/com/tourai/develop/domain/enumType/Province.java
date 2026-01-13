package com.tourai.develop.domain.enumType;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Province {
    SEOUL,
    BUSAN,
    JEJU;

    @JsonCreator
    public static Province from(String value) {
        if (value == null) return null;
        return Province.valueOf(value.toUpperCase());
    }
}
