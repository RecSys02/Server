package com.tourai.develop.dto.request;

import com.tourai.develop.domain.enumType.Province;
import com.tourai.develop.dto.SelectedPlaceDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record PlanRequestDto(
        @NotNull(message = "최소 하나 이상의 장소를 선택하여야 합니다.")
        List<SelectedPlaceDto> selectedPlaces,

        @NotNull(message = "Plan 이름은 필수입니다.")
        String name,

        @NotNull(message = "여행 시작 날짜는 필수입니다.")
        LocalDate startDate,

        @NotNull(message = "여행 종료 날짜는 필수입니다.")
        LocalDate endDate,

        @NotNull(message = "Province는 필수입니다.")
        Province province,

        @NotNull(message = "공개 여부는 필수입니다.")
        Boolean isPrivate
) { }
