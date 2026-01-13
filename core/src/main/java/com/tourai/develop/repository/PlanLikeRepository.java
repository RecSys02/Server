package com.tourai.develop.repository;

import com.tourai.develop.domain.entity.Plan;
import com.tourai.develop.domain.entity.PlanLike;
import com.tourai.develop.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanLikeRepository extends JpaRepository<PlanLike, Long> {
    Optional<PlanLike> findByUserAndPlan(User user, Plan plan);
    long countByPlan(Plan plan);
}
