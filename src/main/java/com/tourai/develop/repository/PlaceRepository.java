package com.tourai.develop.repository;

import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findByPlaceId(Long placeId);
    Optional<Place> findByPlaceIdAndCategoryAndPlaceRegion(Long placeId, Category category, Region placeRegion);
    List<Place> findAllByPlaceIdIn(List<Long> placeIds);
}
