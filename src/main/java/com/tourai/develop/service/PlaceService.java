package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.dto.PlaceInfo;
import com.tourai.develop.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;

    /**
     * 내부 데이터 동기화 메서드
     * 1. 리스트에 없는 DB 데이터는 삭제 (Delete)
     * 2. DB에 없으면 추가 (Insert)
     * 3. DB에 있으면 정보 갱신 (Update)
     */
    @Transactional
    public void syncPlaces(List<PlaceInfo> placeInfos) {
        // 1. 입력받은 데이터들의 ID 집합 생성
        Set<Long> newPlaceIds = placeInfos.stream()
                .map(PlaceInfo::placeId)
                .collect(Collectors.toSet());

        // 2. DB에 존재하지만 입력 리스트에는 없는(삭제 대상) 데이터 식별 및 삭제
        List<Place> allPlaces = placeRepository.findAll();
        List<Place> placesToDelete = allPlaces.stream()
                .filter(place -> !newPlaceIds.contains(place.getPlaceId()))
                .toList();

        if (!placesToDelete.isEmpty()) {
            placeRepository.deleteAll(placesToDelete);
            log.info("Deleted {} places that are no longer in the list.", placesToDelete.size());
        }

        // 3. 추가 또는 갱신 (Upsert)
        for (PlaceInfo info : placeInfos) {
            placeRepository.findByPlaceId(info.placeId())
                    .ifPresentOrElse(
                            existingPlace -> {
                                // 이미 존재하면 정보 업데이트
                                existingPlace.update(
                                        info.region(),
                                        info.name(),
                                        info.description(),
                                        info.pictures(),
                                        info.latitude(),
                                        info.longitude()
                                );
                                log.debug("Place updated: {} (id: {})", info.name(), info.placeId());
                            },
                            () -> {
                                // 존재하지 않으면 새로 저장
                                Place newPlace = Place.builder()
                                        .placeId(info.placeId())
                                        .placeRegion(info.region())
                                        .name(info.name())
                                        .description(info.description())
                                        .picture(info.pictures())
                                        .latitude(info.latitude())
                                        .longitude(info.longitude())
                                        .build();
                                placeRepository.save(newPlace);
                                log.info("New Place registered: {} (id: {})", info.name(), info.placeId());
                            }
                    );
        }
    }
}