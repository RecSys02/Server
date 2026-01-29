package com.tourai.develop.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlanRankingRedisService {

    private final StringRedisTemplate redisTemplate;
    @Value("${app.ranking.key-prefix}")
    private String keyPrefix;

    public void incrementLikeScore(Long planId, double delta) {
        redisTemplate.opsForZSet()
                .incrementScore(allKey(), planId.toString(), delta);
    }

    public void decreaseLikeScore(Long planId, double delta) {
        Double score = redisTemplate.opsForZSet()
                .incrementScore(allKey(), planId.toString(), -delta);
        if (score != null && score <= 0) {
            redisTemplate.opsForZSet()
                    .add(allKey(), planId.toString(), 0.0);
        }

    }
    public List<Long> getTopKPlans(int k) {
        Set<String> topKPlanIds = redisTemplate.opsForZSet()
                .reverseRange(allKey(), 0, k - 1);
        if (topKPlanIds == null || topKPlanIds.isEmpty()) return List.of();
        return topKPlanIds.stream().map(Long::valueOf).toList();
    }

    private String allKey() {
        return keyPrefix + ":all";
    }
}
