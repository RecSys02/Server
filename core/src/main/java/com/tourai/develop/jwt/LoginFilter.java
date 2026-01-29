package com.tourai.develop.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.dto.CustomUserDetails;
import com.tourai.develop.dto.LoginDto;
import com.tourai.develop.exception.enumType.ErrorCode;
import com.tourai.develop.repository.UserRepository;
import com.tourai.develop.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshTokenService refreshTokenService, AuthService authService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("LoginFilter.attemptAuthentication 들어왔음!");
        if (!"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("POST 형식의 메서드 타입이 아닙니다.");
        }

        log.info("LoginFilter.attemptAuthentication 진입 - 정상 로그인 시도");
        try {
            LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
            if (loginDto.getEmail() == null || loginDto.getEmail().isBlank()
                    || loginDto.getPassword() == null || loginDto.getPassword().isBlank()) {
                throw new AuthenticationServiceException("email,password are required");
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword(), Collections.emptyList());

            return authenticationManager.authenticate(authentication);

        } catch (IOException e) {
            throw new AuthenticationServiceException("Invalid login request body", e);
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        log.info("LoginFilter.successfulAuthentication 들어왔음!");

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        String username = principal.getUsername();
        Long userId = principal.getUserId();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        Long accessTokenExpiredMs = 60 * 60 * 10L;
        Long refreshTokenExpiredMs = 864 * 100000L;

        String accessToken = jwtUtil.createJwt("access", userId, username, role, accessTokenExpiredMs);
        String refreshToken = jwtUtil.createJwt("refresh", userId, username, role, refreshTokenExpiredMs);

        refreshTokenService.save(username, refreshToken, Duration.ofMillis(refreshTokenExpiredMs));

        // 로그인 로그 남기기 (AuthService를 통해 AOP 적용)
        Optional<User> userOptional = userRepository.findByEmail(username);
        if (userOptional.isPresent()) {
            authService.onLoginSuccess(userOptional.get());
        } else {
            log.warn("Login successful but user not found in DB for logging: {}", username);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.addCookie(jwtUtil.createCookie("refresh", refreshToken, refreshTokenExpiredMs));
        objectMapper.writeValue(response.getWriter(), Map.of("accessToken", accessToken));

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("LoginFilter.unsuccessfulAuthentication 들어왔음!");
        ErrorCode errorCode = parseFailureToErrorCode(failed);
        sendLoginFilterExceptionResponse(response, errorCode);
    }

    private void sendLoginFilterExceptionResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        if (response.isCommitted()) return;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), Map.of(
                "code", errorCode.name(),
                "message", errorCode.getMessage()
        ));
    }

    private ErrorCode parseFailureToErrorCode(AuthenticationException failed) {
        if (failed instanceof AuthenticationServiceException) {
            return ErrorCode.AUTH_INVALID_REQUEST;
        }
        if (failed instanceof BadCredentialsException) {
            return ErrorCode.AUTH_LOGIN_FAILED;
        }
        return ErrorCode.AUTH_LOGIN_FAILED;
    }


}
