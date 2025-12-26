package com.tourai.develop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.entity.UserLog;
import com.tourai.develop.domain.enumType.Action;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;
import com.tourai.develop.dto.request.PlanRequestDto;
import com.tourai.develop.repository.PlaceRepository;
import com.tourai.develop.repository.UserLogRepository;
import com.tourai.develop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserLogIntegrationTest {

    @Autowired
    private PlanService planService;

    @Autowired
    private UserLogRepository userLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Place testPlace1;
    private Place testPlace2;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        testUser = User.builder()
                .email("test@example.com")
                .userName("TestUser")
                .password("password")
                .build();
        userRepository.save(testUser);

        // 테스트용 장소 생성
        testPlace1 = Place.builder()
                .placeId(1001L)
                .name("Test Place 1")
                .category(Category.RESTAURANT)
                .province(Province.JEJU)
                .address("Jeju Address 1")
                .build();
        placeRepository.save(testPlace1);

        testPlace2 = Place.builder()
                .placeId(1002L)
                .name("Test Place 2")
                .category(Category.CAFE)
                .province(Province.JEJU)
                .address("Jeju Address 2")
                .build();
        placeRepository.save(testPlace2);

        // SecurityContext에 인증 정보 설정 (AOP가 User를 찾을 수 있도록)
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .roles("USER")
                .build();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Plan 생성 시 UserLog가 자동으로 저장되어야 한다")
    void savePlan_ShouldCreateUserLog() {
        // given
        PlanRequestDto requestDto = PlanRequestDto.builder()
                .userId(testUser.getId())
                .placeIds(List.of(testPlace1.getPlaceId(), testPlace2.getPlaceId())) // 실제 생성된 Place ID 사용
                .tagIds(Collections.emptyList())
                .name("제주도 여행")
                .duration(1)
                .province(Province.JEJU)
                .isPrivate(false)
                .build();

        // when
        planService.savePlan(requestDto);

        // then
        List<UserLog> logs = userLogRepository.findAll();
        assertThat(logs).hasSize(1);

        UserLog log = logs.get(0);
        assertThat(log.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(log.getAction()).isEqualTo(Action.CREATE_PLAN);

        // Metadata 검증
        Map<String, Object> metadata = log.getMetadata();
        assertThat(metadata).containsEntry("title", "제주도 여행");
        assertThat(metadata).containsKey("plan_id");
        assertThat(metadata).containsKey("client_ip"); // 공통 필드 확인
        
        System.out.println("Saved Metadata: " + metadata);
    }
}
