package com.tourai.develop.dto;

import lombok.Builder;
import lombok.Getter;


public record EditProfileDto(
        String userName,
        String password
) {
}
