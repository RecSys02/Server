package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.domain.entity.Plan;
import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;
import com.tourai.develop.domain.enumType.TagType;
import com.tourai.develop.dto.PlaceItem;
import com.tourai.develop.dto.SelectedPlaceDto;
import com.tourai.develop.dto.request.PlanRequestDto;
import com.tourai.develop.repository.PlaceRepository;
import com.tourai.develop.repository.PlanRepository;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Transactional
public class PlanServiceTest {

    @Autowired PlanAiService  planAiService;
    @Autowired PlaceRepository placeRepository;
    @Autowired PlanRepository planRepository;
    @Autowired PlanService planService;
    @Autowired UserRepository userRepository;
    @Autowired TagRepository tagRepository;
    @Autowired EntityManager em;

    @Test
    void togglePlanLikeTest() {
        // User 생성
        User user = User.builder()
                .userName("TestUser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(user);

        // Plan 생성
        Plan plan = Plan.builder()
                .user(user)
                .name("Test Plan")
                .isPrivate(false)
                .build();
        planRepository.save(plan);

        // 영속성 컨텍스트 초기화 (데이터베이스 반영 확인을 위해)
        em.flush();
        em.clear();

        // 1. 좋아요 추가
        planService.addPlanLike(plan.getId(), user.getEmail());

        em.flush();
        em.clear();

        Plan likedPlan = planRepository.findById(plan.getId()).orElseThrow();
        Assertions.assertThat(likedPlan.getLikeCount()).isEqualTo(1);

        // 2. 좋아요 취소
        planService.removePlanLike(plan.getId(), user.getEmail());

        em.flush();
        em.clear();

        Plan unlikedPlan = planRepository.findById(plan.getId()).orElseThrow();
        Assertions.assertThat(unlikedPlan.getLikeCount()).isEqualTo(0);
    }

    @Test
    void promptTest() {

        Place place1 = Place.builder()
                .placeId(99991L)
                .name("Place1")
                .province(Province.SEOUL)
                .address("address")
                .category(Category.TOURSPOT)
                .description("Place 1 Description")
                .build();
        placeRepository.save(place1);

        Place place2 = Place.builder()
                .placeId(99992L)
                .name("Place2")
                .province(Province.SEOUL)
                .address("address")
                .category(Category.TOURSPOT)
                .description("Place 2 Description")
                .build();
        placeRepository.save(place2);

        List<SelectedPlaceDto> selectedPlaces = Arrays.asList(
                new SelectedPlaceDto(99991L, Category.TOURSPOT, Province.SEOUL),
                new SelectedPlaceDto(99992L, Category.TOURSPOT, Province.SEOUL)
        );
        Integer duration = 1;

        System.out.println(planAiService.makePromptFromPlaces(selectedPlaces, duration));
    }

    @Test
    void convertMapFromStringTest() {
        String jsonString = """
                {
                  "1": [
                    {
                      "time": "12:00-13:00",
                      "place_id": 1,
                      "province": "SEOUL",
                      "category": "TOURSPOT",
                      "place_name": "place 1"
                    },
                    {
                      "time": "14:00-15:00",
                      "place_id": 2,
                      "province": "SEOUL",
                      "category": "TOURSPOT",
                      "place_name": "place 2"
                    },
                    {
                      "time": "17:00-18:00",
                      "place_id": 3,
                      "province": "SEOUL",
                      "category": "TOURSPOT",
                      "place_name": "place 3"
                    }
                  ],
                  "2": [
                    {
                      "time": "10:00-11:00",
                      "place_id": 4,
                      "province": "SEOUL",
                      "category": "TOURSPOT",
                      "place_name": "place 4"
                    },
                    {
                      "time": "11:00-13:00",
                      "place_id": 5,
                      "province": "SEOUL",
                      "category": "TOURSPOT",
                      "place_name": "place 5"
                    }
                  ]
                }
                """;
        Assertions.assertThat(planAiService.convertMapFromString(jsonString)).isInstanceOf(Map.class);
    }

    @Test
    void createScheduleTest() {
        Place place1 = Place.builder()
                .placeId(99991L)
                .name("Place1")
                .province(Province.SEOUL)
                .address("address")
                .category(Category.TOURSPOT)
                .description("Place 1 Description")
                .build();
        placeRepository.save(place1);

        Place place2 = Place.builder()
                .placeId(99992L)
                .name("Place2")
                .province(Province.SEOUL)
                .address("address")
                .category(Category.TOURSPOT)
                .description("Place 2 Description")
                .build();
        placeRepository.save(place2);

        List<SelectedPlaceDto> selectedPlaces = Arrays.asList(
                new SelectedPlaceDto(99991L, Category.TOURSPOT, Province.SEOUL),
                new SelectedPlaceDto(99992L, Category.TOURSPOT, Province.SEOUL)
        );
        Integer duration = 1;

        Map<String, List<PlaceItem>> stringListMap = planAiService.createSchedule(selectedPlaces, duration);
        stringListMap.forEach((key, value) -> {
            Assertions.assertThat(key).isEqualTo("1");
            value.forEach(item -> Assertions.assertThat(item).isInstanceOf(PlaceItem.class));
        });
    }

    @Test
    void savePlanTest() {

        // User
        User user = User.builder()
                .userName("테스트")
                .email("asdf@asdf.com")
                .password("asdfasdf")
                .build();
        userRepository.save(user);

        System.out.println(user.getId());

        // Places
        Place place1 = Place.builder()
                .placeId(99991L)
                .name("Place1")
                .province(Province.SEOUL)
                .address("address")
                .category(Category.TOURSPOT)
                .description("Place 1 Description")
                .build();
        placeRepository.save(place1);

        Place place2 = Place.builder()
                .placeId(99992L)
                .name("Place2")
                .province(Province.SEOUL)
                .address("address")
                .category(Category.TOURSPOT)
                .description("Place 2 Description")
                .build();
        placeRepository.save(place2);

        List<SelectedPlaceDto> selectedPlaces = Arrays.asList(
                new SelectedPlaceDto(99991L, Category.TOURSPOT, Province.SEOUL),
                new SelectedPlaceDto(99992L, Category.TOURSPOT, Province.SEOUL)
        );

        // Tags (Tag 엔티티는 category가 아니라 tagType을 사용)
        Tag tag1 = Tag.builder()
                .name("Tag1")
                .tagType(TagType.CAFE)
                .build();

        Tag tag2 = Tag.builder()
                .name("Tag2")
                .tagType(TagType.RESTAURANT)
                .build();

        tagRepository.saveAll(List.of(tag1, tag2));
        List<Long> tagIds = List.of(tag1.getId(), tag2.getId());

        // Plan
        PlanRequestDto dto = PlanRequestDto.builder()
                .userId(user.getId())
                .name("Plan1")
                .duration(1)
                .selectedPlaces(selectedPlaces)
                .tagIds(tagIds)
                .province(Province.SEOUL)
                .isPrivate(true)
                .build();

        planService.savePlan(dto);

        List<Plan> plans = planRepository.findAll();
        Plan savedPlan = plans.stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Plan not saved"));

        Assertions.assertThat(savedPlan.getName()).isEqualTo("Plan1");
        Assertions.assertThat(savedPlan.getSchedule()).isNotNull();
        Assertions.assertThat(savedPlan.getPlanTags()).hasSize(2);

        List<String> savedTagNames = savedPlan.getPlanTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList();
        Assertions.assertThat(savedTagNames).contains("Tag1", "Tag2");
    }
}
