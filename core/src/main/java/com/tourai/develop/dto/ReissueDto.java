package com.tourai.develop.dto;

import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class ReissueDto {
    @NonNull
    private final String accessToken;
    @NonNull
    private final String refreshToken;
}
