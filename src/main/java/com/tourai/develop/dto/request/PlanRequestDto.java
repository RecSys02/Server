package com.tourai.develop.dto.request;

import com.tourai.develop.domain.enumType.Region;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record PlanRequestDto(
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "최소 하나 이상의 장소를 선택하여야 합니다.")
        List<Long> placeIds,

        @NotNull(message = "태그가 선택되지 않았습니다.")
        List<Long> tagIds,

        @NotNull(message = "Plan 이름은 필수입니다.")
        String name,

        @NotNull(message = "여행 기간은 필수입니다.")
        @Min(value = 1, message = "여행 기간은 최소 1일 이상이어야 합니다.")
        Integer duration,

        @NotNull(message = "Region은 필수입니다.")
        Region placeRegion,

        @NotNull(message = "공개 여부는 필수입니다.")
        Boolean isPrivate
) { }
