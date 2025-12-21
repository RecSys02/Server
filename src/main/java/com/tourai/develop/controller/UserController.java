package com.tourai.develop.controller;

import com.tourai.develop.dto.LoginDto;
import com.tourai.develop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 사용하여 로그인합니다. 실제 처리는 Spring Security 필터에서 이루어집니다.")
    @PostMapping("/login")
    public void login(@RequestBody LoginDto loginDto) {
    }

}
