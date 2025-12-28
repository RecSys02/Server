package com.tourai.develop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.client.genai.TextGenerator;
import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.dto.DailySchedule;
import com.tourai.develop.dto.SelectedPlaceDto;
import com.tourai.develop.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanAiService {

    private final PlaceRepository placeRepository;
    private final TextGenerator textGenerator;
    private final String planInstructor;
    private final ObjectMapper objectMapper;

    public List<DailySchedule> createSchedule(List<SelectedPlaceDto> selectedPlaces, LocalDate startDate, Integer duration) {

        String prompt = makePromptFromPlaces(selectedPlaces, startDate, duration);

        String jsonString = textGenerator.generate("gemini-2.5-flash", planInstructor, prompt);

        return convertListFromString(jsonString);
    }

    public String makePromptFromPlaces(List<SelectedPlaceDto> selectedPlaces, LocalDate startDate, Integer duration) {

        List<Place> places = new ArrayList<>();
        for (SelectedPlaceDto dto : selectedPlaces) { //TODO: 쿼리 최적화 필요
            placeRepository.findByPlaceIdAndCategoryAndProvince(dto.placeId(), dto.category(), dto.province())
                    .ifPresent(places::add);
        }

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        int size = places.size();

        sb1.append("여행 시작 날짜: ").append(startDate).append("\n");
        sb1.append("여행일수: ").append(duration).append("일\n\n선택한 여행지: ");
        for (int i = 0; i < size; i++) {
            Place place = places.get(i);

            sb1.append(place.getName()).append("(여행지 place_id: ").append(place.getPlaceId()).append(", province: ").append(place.getProvince()).append(", category: ").append(place.getCategory()).append(", 소요시간: ").append(place.getDuration()).append(")").append(", ");
            sb2.append(place.getName()).append(" 여행지 설명: ").append(place.getDescription()).append("\n");

        }
        sb1.append("\n\n").append(sb2);

        return  sb1.toString();
    }

    public List<DailySchedule> convertListFromString(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
