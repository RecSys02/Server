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
        LocalDate endDate = startDate.plusDays(duration - 1);

        sb1.append("여행 시작 날짜: ").append(startDate).append("\n");
        sb1.append("여행 종료 날짜: ").append(endDate).append("\n");
        sb1.append("여행일수: ").append(duration).append("일\n\n[선택한 여행지 목록]");
        for (int i = 0; i < size; i++) {
            Place place = places.get(i);
            sb1.append("\n- [ID:").append(place.getPlaceId()).append("] ");
            sb1.append("[").append(place.getCategory()).append("] ");
            sb1.append(place.getName());
            sb1.append(" (소요시간: ").append(place.getDuration()).append(")");
        }
        sb1.append("\n\n").append(sb2);
        sb1.append("\n\n[작성 목표]");
        sb1.append("\n빈칸 없이 꽉 찬 여행 일정을 만들어주세요. 아래 패턴을 그대로 따라 채워넣으세요.");

        sb1.append("\n\n[패턴 1: 첫날 (").append(startDate).append(")]");
        sb1.append("\n12:00~13:00 점심 식사 (RESTAURANT)");
        sb1.append("\n13:30~15:30 오후 관광 1 (TOURSPOT)");
        sb1.append("\n16:00~17:30 오후 관광 2 (TOURSPOT)");
        sb1.append("\n18:00~19:00 저녁 식사 (RESTAURANT)");
        sb1.append("\n19:30~21:00 야간 관광 (TOURSPOT)");

        sb1.append("\n\n[패턴 2: 중간 날짜]");
        sb1.append("\n08:00~09:00 아침 식사 (RESTAURANT)");
        sb1.append("\n09:30~11:30 오전 관광 (TOURSPOT)");
        sb1.append("\n12:00~13:00 점심 식사 (RESTAURANT)");
        sb1.append("\n13:30~15:30 오후 관광 1 (TOURSPOT)");
        sb1.append("\n16:00~17:30 오후 관광 2 (TOURSPOT)");
        sb1.append("\n18:00~19:00 저녁 식사 (RESTAURANT)");

        sb1.append("\n\n[패턴 3: 마지막 날 (").append(endDate).append(")]");
        sb1.append("\n08:00~09:00 아침 식사 (RESTAURANT)");
        sb1.append("\n09:30~11:30 오전 관광 (TOURSPOT)");
        sb1.append("\n12:00~13:00 점심 식사 (RESTAURANT)");
        sb1.append("\n(13:00 이후 일정 종료)");

        sb1.append("\n\n[데이터 무결성 규칙 (절대 준수)]");
        sb1.append("\n1. **ID-이름 일치**: 위 [선택한 여행지 목록]에 있는 [ID]와 [이름]을 반드시 짝지어서 사용하세요.");
        sb1.append("\n   - (X) 1, 2, 3... 처럼 임의로 번호를 매기지 마세요. 원래 ID(예: 773, 1010)를 그대로 쓰세요.");
        sb1.append("\n2. **없는 장소 금지**: 목록에 없는 ID나 이름을 절대 창조하지 마세요.");
        sb1.append("\n3. **중복 규칙**:");
        sb1.append("\n   - **관광지(TOURSPOT)**: 절대 중복 금지. 한 번 간 곳은 다시 가지 마세요.");
        sb1.append("\n   - **식당(RESTAURANT)**: 가능한 한 새로운 곳을 가되, 식당이 부족하면 중복 허용합니다.");
        sb1.append("\n4. **이동 시간 30분**: 모든 활동 사이에는 30분의 이동 시간이 포함되어야 합니다.");

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
