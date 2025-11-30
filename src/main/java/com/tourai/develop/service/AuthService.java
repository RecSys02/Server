package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.dto.SignUpDto;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    // 로그인, 회원가입 관련 메서드 구현

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void singUp(SignUpDto signUpDto) {

        //이메일 중복 확인
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다!");
        }

        //닉네임 중복 확인
        if (userRepository.existsByUserName(signUpDto.getUserName())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임 입니다!");
        }
        validatePassword(signUpDto.getPassword());

        String encodedPassword = bCryptPasswordEncoder.encode(signUpDto.getPassword());


        User user = User.builder().userName(signUpDto.getUserName())
                .email(signUpDto.getEmail())
                .password(encodedPassword).build();


        if (signUpDto.getTagIds() != null) {
            for (Long tagId : signUpDto.getTagIds()) {

                Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Tag id 입니다!"));
                user.addTag(tag);
            }
        }

        userRepository.save(user);

    }

    private void validatePassword(String password) {

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어 합니다!");
        }

        boolean isHasLetter = password.chars().anyMatch(Character::isLetter);
        boolean isHasDigit = password.chars().anyMatch(Character::isDigit);

        if (!isHasLetter || !isHasDigit) {
            throw new IllegalArgumentException("비밀번호는 영문과 숫자를 모두 포함해야 합니다!");
        }
    }


}
