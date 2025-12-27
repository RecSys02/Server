package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;
import com.tourai.develop.dto.PlaceInfo;
import com.tourai.develop.repository.PlaceRepository;
import com.tourai.develop.dto.response.PlaceResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;

    public PlaceResponseDto getPlaceDetail(Long placeId, Category category, Province province) {
        Place place = placeRepository.findByPlaceIdAndCategoryAndProvince(placeId, category, province)
                .orElseThrow(() -> new IllegalArgumentException("Place not found with id: " + placeId + ", category: " + category + ", province: " + province));
        return PlaceResponseDto.from(place);
    }

    /**
     * 내부 데이터 동기화 메서드
     * 1. 리스트에 없는 DB 데이터는 삭제 (Delete)
     * 2. DB에 없으면 추가 (Insert)
     * 3. DB에 있으면 정보 갱신 (Update)
     */
    @Transactional
    public void syncPlaces(List<PlaceInfo> placeInfos) {
        record PlaceKey(Long placeId, Category category, Province province) {}

        // 1. Prepare a map of incoming data for efficient lookup.
        Map<PlaceKey, PlaceInfo> newPlacesMap = placeInfos.stream()
                .collect(Collectors.toMap(
                        info -> new PlaceKey(info.placeId(), info.category(), info.province()),
                        java.util.function.Function.identity(),
                        (existing, replacement) -> replacement // Handle duplicates in source data
                ));

        // 2. Fetch all existing places and map them by their key.
        // Note: findAll() can be a performance bottleneck with very large tables.
        Map<PlaceKey, Place> existingPlacesMap = placeRepository.findAll().stream()
                .collect(Collectors.toMap(
                        p -> new PlaceKey(p.getPlaceId(), p.getCategory(), p.getProvince()),
                        java.util.function.Function.identity()
                ));

        // 3. Determine which places to delete.
        List<Place> placesToDelete = existingPlacesMap.values().stream()
                .filter(p -> !newPlacesMap.containsKey(new PlaceKey(p.getPlaceId(), p.getCategory(), p.getProvince())))
                .toList();

        if (!placesToDelete.isEmpty()) {
            placeRepository.deleteAllInBatch(placesToDelete); // Use batch deletion for performance.
            log.info("Deleted {} places that are no longer in the list.", placesToDelete.size());
        }

        // 4. Determine places to create and update.
        List<Place> placesToCreate = new ArrayList<>();
        for (PlaceInfo info : newPlacesMap.values()) {
            PlaceKey key = new PlaceKey(info.placeId(), info.category(), info.province());
            Place existingPlace = existingPlacesMap.get(key);

            if (existingPlace != null) {
                // Update existing place. Changes will be flushed at transaction commit.
                existingPlace.update(
                        info.category(), info.province(), info.name(), info.address(), info.duration(),
                        info.description(), info.images(), info.keywords(), info.latitude(), info.longitude()
                );
                log.debug("Place updated: {} (id: {}, cat: {}, province: {})", info.name(), info.placeId(), info.category(), info.province());
            } else {
                // Add new place to a list for batch insertion.
                placesToCreate.add(Place.builder()
                        .placeId(info.placeId())
                        .category(info.category())
                        .province(info.province())
                        .name(info.name())
                        .address(info.address())
                        .duration(info.duration())
                        .description(info.description())
                        .images(info.images())
                        .keywords(info.keywords())
                        .latitude(info.latitude())
                        .longitude(info.longitude())
                        .build());
            }
        }

        if (!placesToCreate.isEmpty()) {
            placeRepository.saveAll(placesToCreate); // Use batch insertion for performance.
            log.info("Registered {} new places.", placesToCreate.size());
        }
    }
}