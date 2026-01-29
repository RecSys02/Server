package com.tourai.develop.controller;

import com.tourai.develop.dto.UserContextDto;
import com.tourai.develop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class UserInternalController {

    private final UserService userService;

    @GetMapping("/context/{userId}")
    public ResponseEntity<UserContextDto> getUserContext(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getUserContext(userId));
    }
}
