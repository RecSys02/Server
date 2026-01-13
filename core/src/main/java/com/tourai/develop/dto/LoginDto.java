package com.tourai.develop.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginDto {
    private String email;
    private String password;


    @JsonCreator
    public LoginDto(@JsonProperty("email") String email,
                    @JsonProperty("password") String password) {

        this.email = email;
        this.password = password;
    }
}
