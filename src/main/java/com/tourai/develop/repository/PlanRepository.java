package com.tourai.develop.repository;

import com.tourai.develop.domain.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByIsPrivateFalseAndCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    List<Plan> findTop6ByIsPrivateFalseAndCreatedAtBetweenOrderByLikeCountDesc(LocalDateTime from, LocalDateTime to);

    List<Plan> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime from, LocalDateTime to);
}
