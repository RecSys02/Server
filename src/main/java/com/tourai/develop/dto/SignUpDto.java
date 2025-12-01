package com.tourai.develop.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SignUpDto {
    private String userName;
    private String email;
    private String password;
    private List<Long> tagIds;

}
