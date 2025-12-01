package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.entity.UserTag;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.dto.EditProfileDto;
import com.tourai.develop.dto.EditUserTagsDto;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Rollback(value = false)   //db 확인 가능 용도
class UserServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TagRepository tagRepository;


    @AfterEach
    public void deleteAll() {
        userRepository.deleteAll();
        tagRepository.deleteAll();
    }


    @Test
    void editUserInfo() {
        User user = User.builder().userName("박새결")
                .email("ab123c@naver.com")
                .password("abc123456").build();

        userRepository.save(user);
        EditProfileDto dto = EditProfileDto.builder().userName("new박새결")
                .email("newabc123@naver.com")
                .password("newabc123456").build();

        userService.editUserInfo(user.getId(), dto);

        User findUser = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 입니다!"));

        assertEquals(findUser.getUserName(), "new박새결");
        assertEquals(findUser.getEmail(), "newabc123@naver.com");

    }

    @Test
    void editUserTags() {

        Tag tag1 = Tag.builder()
                .category(Category.cafe)
                .name("카페").build();
        Tag tag2 = Tag.builder()
                .category(Category.restaurant)
                .name("식당").build();
        Tag tag3 = Tag.builder()
                .category(Category.tourSpot)
                .name("관광지").build();
        tagRepository.save(tag1);
        tagRepository.save(tag2);
        tagRepository.save(tag3);

        List<Long> tagIds = new ArrayList<>();
        tagIds.add(tag1.getId());
        tagIds.add(tag2.getId());
        tagIds.add(tag3.getId());

        User user = User.builder().userName("박새결")
                .email("ab123c@naver.com")
                .password("abc123456").build();

        user.addTag(tag1);
        userRepository.save(user);
        EditUserTagsDto dto = EditUserTagsDto.builder()
                .updateTagIds(tagIds).build();

        userService.editUserTags(user.getId(), dto);
        User findUser = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다!"));
        for (UserTag userTag : findUser.getUserTags()) {
            System.out.println("userTag.getTag().getId() = " + userTag.getTag().getId());
        }

        // 1) 태그 개수 검증
        assertEquals(3, findUser.getUserTags().size());

        // 2) 태그 ID 세트가 기대값과 같은지 검증 (순서는 상관없이)
        Set<Long> actualTagIds = findUser.getUserTags().stream()
                .map(userTag -> userTag.getTag().getId())
                .collect(Collectors.toSet());

        Set<Long> expectedTagIds = new HashSet<>(tagIds);

        assertEquals(expectedTagIds, actualTagIds);
    }
}