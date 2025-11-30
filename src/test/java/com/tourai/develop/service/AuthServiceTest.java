package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.dto.SignUpDto;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@Rollback(value = false)  //db 확인 가능 용도
@Transactional
class AuthServiceTest {

    @Autowired
    AuthService authService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TagRepository tagRepository;


    @Test
    public void singUp() {

        Tag tag1 = Tag.builder()
                .category(Category.cafe)
                .name("따뜻한").build();

        Tag tag2 = Tag.builder()
                .category(Category.restaurant)
                .name("차가운").build();

        tagRepository.save(tag1);
        tagRepository.save(tag2);

        ArrayList<Long> list = new ArrayList<>();
        list.add(tag1.getId());
        list.add(tag2.getId());

        SignUpDto signUpDto = SignUpDto.builder().userName("박새결")
                .email("a123@naver.com")
                .password("a123456789")
                .tagIds(list)
                .build();


        authService.singUp(signUpDto);

        User savedUser = userRepository.findByEmail(signUpDto.getEmail()).orElseThrow(() -> new IllegalArgumentException("해당 email을 가진 user가 존재하지 않습니다!"));
        assertEquals(signUpDto.getUserName(), savedUser.getUserName());
        assertEquals(2, savedUser.getUserTags().size());
    }


}