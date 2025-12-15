package com.tourai.develop.repository;

import com.tourai.develop.domain.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findByPlaceId(Long placeId);
    List<Place> findAllByPlaceIdIn(List<Long> placeIds);
}
