package com.tourai.develop.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;


    private String getKeyConvention(String userKey) {
        return "refresh:" + userKey;
    }

    public void save(String userKey, String refreshToken, Duration duration) {
        redisTemplate.opsForValue().set(getKeyConvention(userKey), refreshToken, duration);
    }

    public String getValue(String userKey) {
        return redisTemplate.opsForValue().get(getKeyConvention(userKey));
    }

    public void delete(String userKey) {
        redisTemplate.delete(getKeyConvention(userKey));
    }

    public boolean isMatch(String userKey, String refreshToken) {
        String savedRefreshToken = getValue(userKey);
        return savedRefreshToken != null && savedRefreshToken.equals(refreshToken);
    }


}
