package com.tourai.develop.dto.response;

import com.tourai.develop.domain.entity.PlanLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PlanLogResponseDto {
    private Long id;
    private String selectedPlaces;
    private String thinkingContent;
    private String schedule;
    private LocalDateTime createdAt;

    public static PlanLogResponseDto from(PlanLog planLog) {
        return PlanLogResponseDto.builder()
                .id(planLog.getId())
                .selectedPlaces(planLog.getSelectedPlaces())
                .thinkingContent(planLog.getThinkingContent())
                .schedule(planLog.getSchedule())
                .createdAt(planLog.getCreatedAt())
                .build();
    }
}
