package com.tourai.develop.controller;

import com.tourai.develop.dto.*;
import com.tourai.develop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PatchMapping("/profile")
    public ResponseEntity<?> editProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody EditProfileDto dto) {
        Long userId = customUserDetails.getUserId();
        userService.editUserInfo(userId, dto);
        return ResponseEntity.ok(Map.of(
                "message", "회원정보 수정 성공"
        ));
    }


    @PutMapping("/tags")
    public ResponseEntity<?> editUserTags(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody EditUserTagsDto dto) {
        Long userId = customUserDetails.getUserId();
        userService.editUserTags(userId, dto);
        return ResponseEntity.ok(Map.of(
                "message", "회원태그 수정 성공"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMe(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        return ResponseEntity.ok(userService.getMe(userId));
    }
}
