package com.tourai.develop.repository;

import com.tourai.develop.domain.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByIsPrivateFalseAndCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    List<Plan> findTop6ByIsPrivateFalseAndCreatedAtBetweenOrderByLikeCountDesc(LocalDateTime from, LocalDateTime to);

    List<Plan> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime from, LocalDateTime to);


    @Query(
            value = """
                    SELECT *
                    FROM plan
                    WHERE id = ANY(:ids)
                      AND is_private = false
                      AND created_at BETWEEN :from AND :to
                    ORDER BY array_position(:ids, id)
                    """,
            nativeQuery = true
    )
    List<Plan> findPopularPlansByIdsOrdered(
            @Param("ids") Long[] ids,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

}
