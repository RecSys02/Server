package com.tourai.develop.controller;

import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;
import com.tourai.develop.dto.response.PlaceResponseDto;
import com.tourai.develop.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
@Tag(name = "Place", description = "Place 관련 API")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/detail")
    @Operation(summary = "Place 상세 조회", description = "placeId, category, province를 이용하여 장소의 상세 정보를 조회합니다.")
    public ResponseEntity<PlaceResponseDto> getPlaceDetail(
            @RequestParam Long placeId,
            @RequestParam Category category,
            @RequestParam Province province) {
        return ResponseEntity.ok(placeService.getPlaceDetail(placeId, category, province));
    }
}
