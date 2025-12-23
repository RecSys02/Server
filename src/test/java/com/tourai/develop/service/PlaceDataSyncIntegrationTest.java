package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.repository.PlaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlaceDataSyncIntegrationTest {

    @Autowired
    private PlaceDataSyncService placeDataSyncService;

    @Autowired
    private PlaceRepository placeRepository;

    @Test
    @DisplayName("Integration Test: Sync JSON to DB and verify fields")
    void verifySyncToDatabase() {
        // given
        String testJsonPath = "data/place/poi_seoul_251222.json";

        // when
        placeDataSyncService.syncFromJson(testJsonPath);

        // then
        List<Place> places = placeRepository.findAll();
        assertThat(places).isNotEmpty();

        // Check Place ID 1
        Place place1 = places.stream()
                .filter(p -> p.getPlaceId().equals(1L))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Place ID 1 not found"));

        // Assertions based on expected JSON content
        assertThat(place1.getCategory()).isEqualTo(Category.TOURSPOT);
        assertThat(place1.getDuration()).isEqualTo("2~3시간");
        assertThat(place1.getImages()).isNotEmpty();
        assertThat(place1.getImages().get(0)).contains("http");
    }
}
