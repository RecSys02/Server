package com.tourai.develop.controller;

import com.tourai.develop.dto.request.PlanRequestDto;
import com.tourai.develop.dto.response.PlanResponseDto;
import com.tourai.develop.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/plans")
@Tag(name = "Plan", description = "Plan 관련 API")
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @Operation(summary = "Plan 생성", description = "생성형 AI를 이용해 새로운 여행 계획을 생성합니다.")
    public ResponseEntity<Void> createPlan(
            @RequestBody @Valid PlanRequestDto planRequestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        planService.savePlan(planRequestDto, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "공개 Plan 조회", description = "모든 사용자의 공개된 Plan을 최신순으로 조회합니다.")
    public ResponseEntity<List<PlanResponseDto>> getPublicPlans() {
        return ResponseEntity.ok(planService.getPublicPlans());
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Plan 상세 조회", description = "특정 Plan의 상세 정보를 조회합니다.")
    public ResponseEntity<PlanResponseDto> getPlan(@PathVariable Long planId) {
        return ResponseEntity.ok(planService.getPlanDetail(planId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "유저별 Plan 조회", description = "특정 유저가 작성한 Plan 목록을 조회합니다.")
    public ResponseEntity<List<PlanResponseDto>> getUserPlans(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(planService.getUserPlans(userId, userDetails.getUsername()));
    }

    @PatchMapping("/{planId}/privacy")
    @Operation(summary = "Plan 공개 여부 수정", description = "Plan의 공개/비공개 상태를 변경합니다.")
    public ResponseEntity<Void> updatePlanPrivacy(
            @PathVariable Long planId,
            @RequestParam boolean isPrivate) {
        planService.updatePlanPrivateStatus(planId, isPrivate);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "Plan 삭제", description = "특정 Plan을 삭제합니다.")
    public ResponseEntity<Void> deletePlan(@PathVariable Long planId) {
        planService.deletePlan(planId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{planId}/like")
    @Operation(summary = "Plan 좋아요", description = "Plan에 좋아요를 누릅니다.")
    public ResponseEntity<Void> addLike(
            @PathVariable Long planId,
            @AuthenticationPrincipal UserDetails userDetails) {
        planService.addPlanLike(planId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{planId}/like")
    @Operation(summary = "Plan 좋아요 취소", description = "Plan에 누른 좋아요를 취소합니다.")
    public ResponseEntity<Void> removeLike(
            @PathVariable Long planId,
            @AuthenticationPrincipal UserDetails userDetails) {
        planService.removePlanLike(planId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}