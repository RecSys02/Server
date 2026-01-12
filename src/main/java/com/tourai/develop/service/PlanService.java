package com.tourai.develop.service;

import com.tourai.develop.aop.annotation.UserActionLog;
import com.tourai.develop.domain.entity.*;
import com.tourai.develop.domain.enumType.Action;
import com.tourai.develop.dto.DailySchedule;
import com.tourai.develop.dto.SelectedPlaceDto;
import com.tourai.develop.dto.request.PlanRequestDto;
import com.tourai.develop.dto.response.PlanResponseDto;
import com.tourai.develop.repository.PlaceRepository;
import com.tourai.develop.repository.PlanLikeRepository;
import com.tourai.develop.repository.PlanRepository;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final PlanLikeRepository planLikeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PlaceRepository placeRepository;

    private final PlanAiService planAiService;

    public PlanResponseDto getPlanDetail(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 planId 입니다: " + planId));
        return PlanResponseDto.from(plan);
    }

    public List<PlanResponseDto> getPublicPlans(LocalDate from, LocalDate to) {
        LocalDateTime[] dateRange = getDateRange(from, to);
        return planRepository.findByIsPrivateFalseAndCreatedAtBetweenOrderByCreatedAtDesc(dateRange[0], dateRange[1]).stream()
                .map(PlanResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PlanResponseDto> getPopularPlans(LocalDate from, LocalDate to) {
        LocalDateTime[] dateRange = getDateRange(from, to);
        return planRepository.findTop6ByIsPrivateFalseAndCreatedAtBetweenOrderByLikeCountDesc(dateRange[0], dateRange[1]).stream()
                .map(PlanResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PlanResponseDto> getMyPlans(String email, LocalDate from, LocalDate to) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        LocalDateTime[] dateRange = getDateRange(from, to);

        // 본인의 Plan 조회 (모든 Plan)
        return planRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(user.getId(), dateRange[0], dateRange[1]).stream()
                .map(PlanResponseDto::from)
                .collect(Collectors.toList());
    }

    private LocalDateTime[] getDateRange(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime;
        LocalDateTime toDateTime;

        if (from == null && to == null) {
            // 날짜가 없으면 오늘 날짜 이후 (오늘 00:00:00 부터)
            fromDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            toDateTime = LocalDateTime.MAX; // 아주 먼 미래
        } else {
            // from이 없으면 아주 먼 과거부터
            fromDateTime = (from != null) ? LocalDateTime.of(from, LocalTime.MIN) : LocalDateTime.MIN;
            // to가 없으면 아주 먼 미래까지
            toDateTime = (to != null) ? LocalDateTime.of(to, LocalTime.MAX) : LocalDateTime.MAX;
        }
        return new LocalDateTime[]{fromDateTime, toDateTime};
    }

    @UserActionLog(action = Action.CREATE_PLAN)
    @Transactional
    public Plan savePlan(PlanRequestDto planRequestDto, String email) {
        User findUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + email));

        // 여행 기간 계산 (종료일 - 시작일 + 1)
        long duration = ChronoUnit.DAYS.between(planRequestDto.startDate(), planRequestDto.endDate()) + 1;
        if (duration < 1) {
            throw new IllegalArgumentException("종료 날짜는 시작 날짜보다 같거나 이후여야 합니다.");
        }

        // 스케줄 생성
        List<DailySchedule> schedule = planAiService.createSchedule(planRequestDto.selectedPlaces(), planRequestDto.startDate(), (int) duration);

        // 이미지 소스 결정
        String imgSrc = planRequestDto.imgSrc();
        if (imgSrc == null || imgSrc.isEmpty()) {
            for (SelectedPlaceDto placeDto : planRequestDto.selectedPlaces()) {
                Place place = placeRepository.findByPlaceIdAndCategoryAndProvince(placeDto.placeId(), placeDto.category(), placeDto.province())
                        .orElse(null);
                if (place != null && !place.getImages().isEmpty()) {
                    imgSrc = place.getImages().get(0);
                    break;
                }
            }
        }

        // plan 생성
        Plan plan = Plan.builder()
                .user(findUser)
                .name(planRequestDto.name())
                .imgSrc(imgSrc)
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
