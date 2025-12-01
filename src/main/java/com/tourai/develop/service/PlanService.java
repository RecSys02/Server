package com.tourai.develop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.domain.entity.Plan;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.dto.request.PlanRequestDto;
import com.tourai.develop.repository.PlaceRepository;
import com.tourai.develop.repository.PlanLikeRepository;
import com.tourai.develop.repository.PlanRepository;
import com.tourai.develop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PlaceRepository placeRepository;

    @Transactional
    public void savePlan(PlanRequestDto planRequestDto) {
        User findUser = userRepository.findById(planRequestDto.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId 입니다!"));

        // TODO: Places로 Prompt 만들어야 함
        List<Place> places = placeRepository.findAllByPlaceIdIn(planRequestDto.placeIds());

        // TODO: LLM API 호출을 통해 JSON을 받아와야 함
        String jsonString = "{}"; // 임시 더미 데이터

        Map<Integer, Map<String, String>> scheduleMap;
        try {
            scheduleMap = objectMapper.readValue(jsonString, new TypeReference<Map<Integer, Map<String, String>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 오류", e);
        }

        Plan plan = Plan.builder()
                .user(findUser)
                .name(planRequestDto.name())
                .isPrivate(planRequestDto.isPrivate())
                .schedule(scheduleMap)
                .build();
        
        planRepository.save(plan);
    }

    @Transactional
    public void deletePlan(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 planId 입니다: " + planId));
        planRepository.delete(plan);
    }

    @Transactional
    public void updatePlanPrivateStatus(Long planId, boolean isPrivate) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 planId 입니다: " + planId));
        plan.updatePrivateStatus(isPrivate);

    }

}