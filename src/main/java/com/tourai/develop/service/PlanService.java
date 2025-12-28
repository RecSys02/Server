package com.tourai.develop.service;

import com.tourai.develop.aop.annotation.UserActionLog;
import com.tourai.develop.domain.entity.*;
import com.tourai.develop.domain.enumType.Action;
import com.tourai.develop.dto.PlaceItem;
import com.tourai.develop.dto.request.PlanRequestDto;
import com.tourai.develop.dto.response.PlanResponseDto;
import com.tourai.develop.repository.PlanLikeRepository;
import com.tourai.develop.repository.PlanRepository;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final PlanLikeRepository planLikeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    private final PlanAiService planAiService;

    public PlanResponseDto getPlanDetail(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 planId 입니다: " + planId));
        return PlanResponseDto.from(plan);
    }

    public List<PlanResponseDto> getPublicPlans() {
        return planRepository.findByIsPrivateFalseOrderByCreatedAtDesc().stream()
                .map(PlanResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PlanResponseDto> getUserPlans(Long targetUserId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + requesterEmail));

        if (requester.getId().equals(targetUserId)) {
            // 본인의 Plan 조회 (모든 Plan)
            return planRepository.findByUserIdOrderByCreatedAtDesc(targetUserId).stream()
                    .map(PlanResponseDto::from)
                    .collect(Collectors.toList());
        } else {
            // 타인의 Plan 조회 (공개된 Plan만)
            return planRepository.findByUserIdAndIsPrivateFalseOrderByCreatedAtDesc(targetUserId).stream()
                    .map(PlanResponseDto::from)
                    .collect(Collectors.toList());
        }
    }

    @UserActionLog(action = Action.CREATE_PLAN)
    @Transactional
    public Plan savePlan(PlanRequestDto planRequestDto, String email) {
        User findUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + email));

        // 스케줄 생성
        Map<String, List<PlaceItem>> schedule = planAiService.createSchedule(planRequestDto.selectedPlaces(), planRequestDto.duration());

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

        return planRepository.save(plan);
    }

    @UserActionLog(action = Action.DELETE_PLAN)
    @Transactional
    public Plan deletePlan(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 planId 입니다: " + planId));
        
        // TODO: 권한 체크 로직 필요 (현재 로그인한 사용자와 plan 작성자가 같은지)
        // SecurityContextHolder를 사용하는 경우 여기서 체크하거나, 
        // Controller에서 체크해서 넘겨줘야 함.
        
        planRepository.delete(plan);
        return plan; // 삭제된 Plan 객체 리턴 (AOP 로깅용)
    }

    @Transactional
    public void updatePlanPrivateStatus(Long planId, boolean isPrivate) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 planId 입니다: " + planId));
        plan.updatePrivateStatus(isPrivate);
    }

    @UserActionLog(action = Action.LIKE_PLAN)
    @Transactional
    public Plan addPlanLike(Long planId, String email) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        if (planLikeRepository.findByUserAndPlan(user, plan).isPresent()) {
            throw new IllegalArgumentException("이미 좋아요를 눌렀습니다.");
        }

        PlanLike newLike = PlanLike.builder()
                .user(user)
                .plan(plan)
                .build();
        planLikeRepository.save(newLike);
        
        return plan;
    }

    @UserActionLog(action = Action.UNLIKE_PLAN)
    @Transactional
    public Plan removePlanLike(Long planId, String email) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        PlanLike existingLike = planLikeRepository.findByUserAndPlan(user, plan)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 누르지 않았습니다."));

        planLikeRepository.delete(existingLike);
        
        return plan; // AOP를 위해 Plan 리턴
    }
}
