package com.example.apigateway.filter;

import com.example.apigateway.jwt.JwtUtil;
import com.example.apigateway.properties.GatewayAuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {


    private final JwtUtil jwtUtil;
    private final List<String> permitPaths;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationGlobalFilter(JwtUtil jwtUtil, GatewayAuthProperties props) {
        this.jwtUtil = jwtUtil;
        this.permitPaths = props.getPermitPaths();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // CORS Preflight는 무조건 통과 (Core와 동일한 CORS 효과를 위해 필수)
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        // permit-path면 토큰 없이 통과
        if (isPermitPath(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "AUTH_REQUIRED", "[GATEWAY] 로그인 후 이용해 주세요.");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange, "AUTH_INVALID", "[GATEWAY] 유효하지 않은 토큰입니다.");
        }
        try {

            jwtUtil.parseAndValidate(token);

            String tokenType = jwtUtil.getTokenType(token);
            if (!"access".equals(tokenType)) {
                return unauthorized(exchange, "AUTH_INVALID", "[GATEWAY] 유효하지 않은 토큰입니다.");
            }
            return chain.filter(exchange);

        } catch (ExpiredJwtException e) {
            return unauthorized(exchange, "AUTH_EXPIRED", "[GATEWAY] 토큰이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange, "AUTH_INVALID", "[GATEWAY] 유효하지 않은 토큰입니다.");
        }

    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPermitPath(String path) {
        for (String permitPath : permitPaths) {
            if (matcher.match(permitPath, path)) return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setCacheControl(CacheControl.noStore());

        try {
            byte[] body = objectMapper.writeValueAsBytes(Map.of(
                    "code", code,
                    "message", message
            ));
            return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
        } catch (Exception e) {
            byte[] body = ("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}").getBytes();
            return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
        }
    }
}
