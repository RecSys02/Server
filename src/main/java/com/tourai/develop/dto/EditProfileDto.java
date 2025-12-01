package com.tourai.develop.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EditProfileDto {
    private String userName;
    private String email;
    private String password;
}
