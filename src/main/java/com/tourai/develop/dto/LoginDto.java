package com.tourai.develop.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginDto {
    private String email;
    private String password;
}
