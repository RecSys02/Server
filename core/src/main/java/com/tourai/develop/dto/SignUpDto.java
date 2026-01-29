package com.tourai.develop.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public record SignUpDto(
        String userName,
        String email,
        String password,
        List<Long> tagIds
) {}
