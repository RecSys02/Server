package com.tourai.develop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.client.genai.GenAiResponse;
import com.tourai.develop.client.genai.TextGenerator;
import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.domain.entity.PlanLog;
import com.tourai.develop.dto.AiScheduleResponse;
import com.tourai.develop.dto.DailySchedule;
import com.tourai.develop.dto.SelectedPlaceDto;
import com.tourai.develop.dto.response.PlanLogResponseDto;
import com.tourai.develop.repository.PlaceRepository;
import com.tourai.develop.repository.PlanLogRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PlanAiService {

    private final PlaceRepository placeRepository;
    private final PlanLogRepository planLogRepository;
    private final TextGenerator textGenerator;
    private final String planInstructor;
    private final ObjectMapper objectMapper;

    public PlanAiService(
            PlaceRepository placeRepository,
            PlanLogRepository planLogRepository,
            @Qualifier("clovaTextGenerator") TextGenerator textGenerator,
            String planInstructor,
            ObjectMapper objectMapper
    ) {
        this.placeRepository = placeRepository;
        this.planLogRepository = planLogRepository;
        this.textGenerator = textGenerator;
        this.planInstructor = planInstructor;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AiScheduleResponse createSchedule(List<SelectedPlaceDto> selectedPlaces, LocalDate startDate, Integer duration) {

        String prompt = makePromptFromPlaces(selectedPlaces, startDate, duration);

        GenAiResponse response = textGenerator.generate("HCX-007", planInstructor, prompt);
        String jsonString = response.getContent();

        AiScheduleResponse scheduleResponse = convertFromString(jsonString);

        savePlanLog(selectedPlaces, response.getThinkingContent(), scheduleResponse.getSchedule());

        return scheduleResponse;
    }

    private void savePlanLog(List<SelectedPlaceDto> selectedPlaces, String thinkingContent, List<DailySchedule> schedule) {
        try {
            String selectedPlacesJson = objectMapper.writeValueAsString(selectedPlaces);
            String scheduleJson = objectMapper.writeValueAsString(schedule);

            PlanLog planLog = PlanLog.builder()
                    .selectedPlaces(selectedPlacesJson)
                    .thinkingContent(thinkingContent)
                    .schedule(scheduleJson)
                    .build();

            planLogRepository.save(planLog);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize plan log data", e);
        }
    }

    public List<PlanLogResponseDto> getAllPlanLogs() {
        return planLogRepository.findAll().stream()
                .map(PlanLogResponseDto::from)
                .collect(Collectors.toList());
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

    public AiScheduleResponse convertFromString(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, AiScheduleResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
