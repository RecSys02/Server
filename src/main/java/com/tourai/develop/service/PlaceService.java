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
        // 주의: placeId가 아예 사라진 경우만 삭제함. 같은 placeId의 다른 카테고리/지역 데이터 정리는 별도 로직 필요 가능성 있음.
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
            placeRepository.findByPlaceIdAndCategoryAndPlaceRegion(info.placeId(), info.category(), info.region())
                    .ifPresentOrElse(
                            existingPlace -> {
                                // 이미 존재하면 정보 업데이트
                                existingPlace.update(
                                        info.category(),
                                        info.region(),
                                        info.name(),
                                        info.address(),
                                        info.duration(),
                                        info.description(),
                                        info.images(),
                                        info.keywords(),
                                        info.latitude(),
                                        info.longitude()
                                );
                                log.debug("Place updated: {} (id: {}, cat: {}, region: {})", info.name(), info.placeId(), info.category(), info.region());
                            },
                            () -> {
                                // 존재하지 않으면 새로 저장
                                Place newPlace = Place.builder()
                                        .placeId(info.placeId())
                                        .category(info.category())
                                        .placeRegion(info.region())
                                        .name(info.name())
                                        .address(info.address())
                                        .duration(info.duration())
                                        .description(info.description())
                                        .images(info.images())
                                        .keywords(info.keywords())
                                        .latitude(info.latitude())
                                        .longitude(info.longitude())
                                        .build();
                                placeRepository.save(newPlace);
                                log.info("New Place registered: {} (id: {}, cat: {}, region: {})", info.name(), info.placeId(), info.category(), info.region());
                            }
                    );
        }
    }
}