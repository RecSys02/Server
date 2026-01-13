package com.tourai.develop.repository;

import com.tourai.develop.domain.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByIsPrivateFalseOrderByCreatedAtDesc();

    List<Plan> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Plan> findByUserIdAndIsPrivateFalseOrderByCreatedAtDesc(Long userId);
}
