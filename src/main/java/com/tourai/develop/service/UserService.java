package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.dto.EditProfileDto;
import com.tourai.develop.dto.EditUserTagsDto;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import com.tourai.develop.validation.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService extends PasswordValidator {
    // 회원정보 관리 메서드 구현
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Transactional
    public void editUserInfo(Long userId, EditProfileDto dto) {

        User findUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId 입니다!"));


        if (dto.getUserName() != null &&
                !findUser.getUserName().equals(dto.getUserName())) {

            if (userRepository.existsByUserName(dto.getUserName())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임 입니다!");
            }
            findUser.changeUserName(dto.getUserName());
        }


        if (dto.getEmail() != null &&
                !findUser.getEmail().equals(dto.getEmail())) {

            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일 입니다!");
            }
            findUser.changeEmail(dto.getEmail());
        }


        if (dto.getPassword() != null) {

            validatePassword(dto.getPassword());

            findUser.changePassword(dto.getPassword(), bCryptPasswordEncoder);
        }


    }

    @Transactional
    public void editUserTags(Long userId, EditUserTagsDto dto) {
        User findUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId 입니다!"));

        findUser.clearTags();

        if (dto.getUpdateTagIds() != null) {
            for (Long tagId : dto.getUpdateTagIds()) {
                Tag findTag = tagRepository.findById(tagId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 TagId 입니다!"));
                findUser.addTag(findTag);
            }
        }
    }


}
