package com.tourai.develop.service;

import com.tourai.develop.domain.entity.*;
import com.tourai.develop.dto.PlaceItem;
import com.tourai.develop.dto.request.PlanRequestDto;
import com.tourai.develop.repository.PlanLikeRepository;
import com.tourai.develop.repository.PlanRepository;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final PlanLikeRepository planLikeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    private final PlanAiService planAiService;

    @Transactional
    public void savePlan(PlanRequestDto planRequestDto) {
        User findUser = userRepository.findById(planRequestDto.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId 입니다!"));

        // 스케줄 생성
        Map<String, List<PlaceItem>> schedule = planAiService.createSchedule(planRequestDto.placeIds(), planRequestDto.duration());

        // plan 생성
        Plan plan = Plan.builder()
                .user(findUser)
                .name(planRequestDto.name())
                .isPrivate(planRequestDto.isPrivate())
                .schedule(schedule)
                .build();

        // plan에 tags 추가
        List<Tag> tags = tagRepository.findAllById(planRequestDto.tagIds());

        for (Tag tag : tags) {
            PlanTag planTag = PlanTag.builder()
                    .plan(plan)
                    .tag(tag)
                    .build();
            plan.addPlanTag(planTag);
        }

        planRepository.save(plan);
    }

    @Transactional
    public void deletePlan(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 planId 입니다: " + planId));
        planRepository.delete(plan);
    }

    @Transactional
    public void updatePlanPrivateStatus(Long planId, boolean isPrivate) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 planId 입니다: " + planId));
        plan.updatePrivateStatus(isPrivate);

    }

    @Transactional
    public void togglePlanLike(Long planId, Long userId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Optional<PlanLike> existingLike = planLikeRepository.findByUserAndPlan(user, plan);

        if (existingLike.isPresent()) {
            // 이미 좋아요를 눌렀다면 -> 취소(삭제)
            planLikeRepository.delete(existingLike.get());
        } else {
            // 좋아요 안 눌렀다면 -> 추가
            PlanLike newLike = PlanLike.builder()
                    .user(user)
                    .plan(plan)
                    .build();
            planLikeRepository.save(newLike);
        }
    }
}