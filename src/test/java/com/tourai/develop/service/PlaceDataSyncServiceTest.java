package com.tourai.develop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;
import com.tourai.develop.dto.PlaceInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlaceDataSyncServiceTest {

    @Mock
    private PlaceService placeService;

    private PlaceDataSyncService placeDataSyncService;

    @BeforeEach
    void setUp() {
        // 실제 ObjectMapper 사용 (JSON 파싱 로직 검증)
        ObjectMapper objectMapper = new ObjectMapper();
        placeDataSyncService = new PlaceDataSyncService(placeService, objectMapper);
    }

    @Test
    @DisplayName("JSON 파일 매핑 테스트: DB 저장 없이 객체 변환 로직만 검증")
    void verifyJsonMappingTest() {
        // given
        String testJsonPath = "data/place/poi_seoul_251224.json";

        // when
        placeDataSyncService.syncFromJson(testJsonPath);

        // then
        // PlaceService.syncPlaces()가 호출되었는지 확인하고, 인자로 넘어간 리스트를 캡처
        ArgumentCaptor<List<PlaceInfo>> captor = ArgumentCaptor.forClass(List.class);
        verify(placeService).syncPlaces(captor.capture());

        List<PlaceInfo> mappedPlaces = captor.getValue();

        // 1. 데이터가 비어있지 않은지 확인
        assertThat(mappedPlaces).isNotEmpty();

        // 2. 특정 데이터(예: 강남 마이스 관광특구) 매핑 검증
        // place_id: 1
        PlaceInfo gangnamMice = mappedPlaces.stream()
                .filter(p -> p.placeId().equals(1L))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Target place not found"));

        assertThat(gangnamMice.name()).isEqualTo("강남 마이스 관광특구");
        assertThat(gangnamMice.category()).isEqualTo(Category.TOURSPOT); // 매핑 로직 확인
        assertThat(gangnamMice.province()).isEqualTo(Province.SEOUL);
        assertThat(gangnamMice.latitude()).isEqualTo(37.5118092746);
        assertThat(gangnamMice.longitude()).isEqualTo(127.0591318945);
        assertThat(gangnamMice.images()).isNotEmpty();
        assertThat(gangnamMice.images().get(0)).contains("http"); // 이미지 URL 확인

        // 3. 설명(Description) 필드 확인
        assertThat(gangnamMice.description()).contains("강남 삼성동 일대");
    }
}
