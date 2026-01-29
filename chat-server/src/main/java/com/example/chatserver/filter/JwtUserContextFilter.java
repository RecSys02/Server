package com.example.chatserver.filter;

import com.example.chatserver.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtUserContextFilter implements WebFilter {

    public static final String CTX_USER_ID = "auth.userId";
    private final JwtUtil jwtUtil;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {


        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (auth == null || !auth.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }


        String accessToken = auth.substring(7);

        try {
            Long userId = jwtUtil.getUserId(accessToken);
            return chain.filter(exchange)
                    .contextWrite(context -> context.put(CTX_USER_ID, userId));

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

    }
}
