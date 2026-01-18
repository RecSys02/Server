package com.tourai.develop.kafka.consumer.ranking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.kafka.event.PlanLikedEvent;
import com.tourai.develop.kafka.event.PlanUnlikedEvent;
import com.tourai.develop.kafka.service.PlanRankingRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlanLikeRankingConsumer {
    private final ObjectMapper objectMapper;
    private final PlanRankingRedisService planRankingRedisService;
    private static final double LIKE_WEIGHT = 1.0;

    @KafkaListener(
            topics = "#{T(com.tourai.develop.kafka.enumType.KafkaTopic).PLAN_LIKED.getTopic()}",
            groupId = "#{T(com.tourai.develop.kafka.enumType.KafkaConsumerGroup).PLAN_RANKING.getGroupId()}"
    )
    public void consumePlanLike(String message) {
        try {
            PlanLikedEvent event = objectMapper.readValue(message, PlanLikedEvent.class);
            planRankingRedisService.incrementLikeScore(event.planId(), LIKE_WEIGHT);
            log.info("[RANK] planlike consume, planId = {}, add amount : {}", event.planId(), LIKE_WEIGHT);
        } catch (Exception e) {
            log.info("[RANK] planlike consume or planRankingRedisService failed...");
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
            topics = "#{T(com.tourai.develop.kafka.enumType.KafkaTopic).PLAN_UNLIKED.getTopic()}",
            groupId = "#{T(com.tourai.develop.kafka.enumType.KafkaConsumerGroup).PLAN_RANKING.getGroupId()}"
    )
    public void consumePlanUnlike(String message) {
        try {
            PlanUnlikedEvent event = objectMapper.readValue(message, PlanUnlikedEvent.class);
            planRankingRedisService.decreaseLikeScore(event.planId(), LIKE_WEIGHT);
            log.info("[RANK] planUnlike consume, planId = {}, subtract amount : {}", event.planId(), LIKE_WEIGHT);
        } catch (Exception e) {
            log.info("[RANK] planUnlike consume or planRankingRedisService failed...");
            throw new RuntimeException(e);
        }
    }


}
