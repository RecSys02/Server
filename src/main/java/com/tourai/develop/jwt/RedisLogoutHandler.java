package com.tourai.develop.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLogoutHandler implements LogoutHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshToken = extractRefreshToken(request);

        try {
            if (refreshToken != null &&
                    !jwtUtil.isExpired(refreshToken)
                    && jwtUtil.getTokenType(refreshToken).equals("refresh")) {

                String email = jwtUtil.getUsername(refreshToken);

                if (refreshTokenService.isMatch(email, refreshToken)) {
                    refreshTokenService.delete(email);
                }

            }

        } catch (Exception e) {
            log.debug("Logout refresh token validation failed: {}", e.getMessage());
        } finally {
            response.addCookie(jwtUtil.deleteCookie("refresh"));
        }


    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh"))
                return cookie.getValue();
        }
        return null;
    }
}
