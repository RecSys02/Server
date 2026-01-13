package com.tourai.develop.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.dto.CustomUserDetails;
import com.tourai.develop.exception.enumType.ErrorCode;
import com.tourai.develop.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {


    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("JwtFilter.doFilterInternal 들어왔음!");
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.info("JwtFilter.doFilterInternal -> access 토큰을 가지고 있지 않으므로, 다음 필터단으로 넘김.");
            filterChain.doFilter(request, response);
            return;
        }


        String accessToken = authorization.split(" ")[1];


        try {
            jwtUtil.parseAndValidate(accessToken);
            String tokenType = jwtUtil.getTokenType(accessToken);
            if (!"access".equals(tokenType)) {
                sendJwtFilterExceptionResponse(response, ErrorCode.AUTH_INVALID);
                return;
            }

            String email = jwtUtil.getUsername(accessToken);
            String role = jwtUtil.getRole(accessToken);

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                    null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            sendJwtFilterExceptionResponse(response, ErrorCode.AUTH_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            sendJwtFilterExceptionResponse(response, ErrorCode.AUTH_INVALID);
        }
    }

    private void sendJwtFilterExceptionResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        if (response.isCommitted()) return;

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), Map.of(
                "code", errorCode.name(),
                "message", errorCode.getMessage()
        ));
    }
}
