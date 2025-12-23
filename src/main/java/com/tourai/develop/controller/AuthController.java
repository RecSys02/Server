package com.tourai.develop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.dto.ReissueDto;
import com.tourai.develop.jwt.CookieUtil;
import com.tourai.develop.service.AuthService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @GetMapping("/")
    public String home() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        return "AuthController 단 도착 -> email : " + email + ", role : " + role;

    }


    @PostMapping("/auth/reissue")
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
}
