package com.tourai.develop.dto.response;

import com.tourai.develop.domain.entity.Plan;
import com.tourai.develop.dto.DailySchedule;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record PlanResponseDto(
        Long id,
        Long userId,
        String userName,
        String name,
        String province,
        Boolean isPrivate,
        Integer likeCount,
        Boolean isActive,
        List<DailySchedule> schedule,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PlanResponseDto from(Plan plan, boolean isActive) {
        return PlanResponseDto.builder()
                .id(plan.getId())
                .userId(plan.getUser().getId())
                .userName(plan.getUser().getUserName())
                .name(plan.getName())
                .province(plan.getProvince())
                .isPrivate(plan.getIsPrivate())
                .likeCount(plan.getLikeCount())
                .isActive(isActive)
                .schedule(plan.getSchedule())
                .tags(plan.getPlanTags().stream()
                        .map(pt -> pt.getTag().getName())
                        .collect(Collectors.toList()))
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
