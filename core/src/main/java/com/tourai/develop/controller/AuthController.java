package com.tourai.develop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.dto.LoginDto;
import com.tourai.develop.dto.ReissueDto;
import com.tourai.develop.dto.SignUpDto;
import com.tourai.develop.jwt.CookieUtil;
import com.tourai.develop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/join")
    public ResponseEntity<?> signUp(@RequestBody SignUpDto signUpDto) {
        log.info("AuthController.signUp 들어왔음!");
        authService.signUp(signUpDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "회원가입 성공!",
                        "email", signUpDto.email(),
                        "userName", signUpDto.userName()
                ));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        log.info("AuthController.reissue 안에 들어왔음!");
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        ReissueDto reissueDto = authService.validateAndReissueToken(refreshToken);
        String newAccessToken = reissueDto.getAccessToken();
        String newRefreshToken = reissueDto.getRefreshToken();
        Long newRefreshTokenExpiredMs = AuthService.refreshTokenExpiredMs;

        response.addCookie(cookieUtil.createCookie("refresh", newRefreshToken, newRefreshTokenExpiredMs));
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 사용하여 로그인합니다. 실제 처리는 Spring Security 필터에서 이루어집니다.")
    @PostMapping("/login")
    public void login(@RequestBody LoginDto loginDto) {
    }

}
