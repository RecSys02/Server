package com.tourai.develop.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.dto.CustomOAuth2User;
import com.tourai.develop.jwt.JwtUtil;
import com.tourai.develop.jwt.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    public final static Long refreshTokenExpiredMs = 864 * 100000L;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String name = customOAuth2User.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();


        String refreshToken = jwtUtil.createJwt("refresh", name, role, refreshTokenExpiredMs);
        response.addCookie(jwtUtil.createCookie("refresh", refreshToken, refreshTokenExpiredMs));
        refreshTokenService.save(name, refreshToken, Duration.ofMillis(refreshTokenExpiredMs));

        response.sendRedirect("http://localhost:8080"); //추후 프론트 서버로 redirect 해야됨 수정 필요!

    }
}
