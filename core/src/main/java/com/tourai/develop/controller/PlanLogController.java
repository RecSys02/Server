package com.tourai.develop.controller;

import com.tourai.develop.dto.response.PlanLogResponseDto;
import com.tourai.develop.service.PlanAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan-logs")
@Tag(name = "Plan Log", description = "Plan 생성 로그 관련 API")
public class PlanLogController {

    private final PlanAiService planAiService;

    @GetMapping
    @Operation(summary = "Plan 생성 로그 전체 조회", description = "AI가 생성한 Plan의 로그(선택한 장소, 생각 과정, 생성된 일정)를 전체 조회합니다.")
    public ResponseEntity<List<PlanLogResponseDto>> getAllPlanLogs() {
        return ResponseEntity.ok(planAiService.getAllPlanLogs());
    }
}
