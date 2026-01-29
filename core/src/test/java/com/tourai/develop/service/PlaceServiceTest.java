package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;
import com.tourai.develop.dto.response.PlaceResponseDto;
import com.tourai.develop.repository.PlaceRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class PlaceServiceTest {

    @Autowired
    PlaceService placeService;

    @Autowired
    PlaceRepository placeRepository;

    @Test
    void getPlaceDetailTest() {
        // Given
        Long placeId = 99999L;
        Category category = Category.TOURSPOT;
        Province province = Province.SEOUL;

        Place place = Place.builder()
                .placeId(placeId)
                .category(category)
                .province(province)
                .name("Test Place")
                .address("Test Address")
                .description("Test Description")
                .build();
        placeRepository.save(place);

        // When
        PlaceResponseDto result = placeService.getPlaceDetail(placeId, category, province);

        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.placeId()).isEqualTo(placeId);
        Assertions.assertThat(result.category()).isEqualTo(category);
        Assertions.assertThat(result.province()).isEqualTo(province);
        Assertions.assertThat(result.name()).isEqualTo("Test Place");
    }
}
