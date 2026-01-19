package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.dto.AvailabilityResponse;
import com.tourai.develop.dto.EditProfileDto;
import com.tourai.develop.dto.EditUserTagsDto;
import com.tourai.develop.dto.UserMeResponse;
import com.tourai.develop.exception.BusinessException;
import com.tourai.develop.exception.enumType.ErrorCode;
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
public class UserService {
    // 회원정보 관리 메서드 구현
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final PasswordValidator passwordValidator;

    @Transactional
    public void editUserInfo(Long userId, EditProfileDto dto) {

        User findUser = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));


        if (dto.userName() != null &&
                !findUser.getUserName().equals(dto.userName())) {

            if (userRepository.existsByUserName(dto.userName())) {
                throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
            }
            findUser.changeUserName(dto.userName());
        }

        if (dto.password() != null) {

            passwordValidator.validatePassword(dto.password());
            findUser.changePassword(dto.password(), bCryptPasswordEncoder);
        }


    }

    @Transactional
    public void editUserTags(Long userId, EditUserTagsDto dto) {
        User findUser = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        findUser.clearTags();

        if (dto.updateTagIds() != null) {
            for (Long tagId : dto.updateTagIds()) {
                Tag findTag = tagRepository.findById(tagId).orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
                findUser.addTag(findTag);
            }
        }
    }

    public UserMeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new UserMeResponse(user.getUserName(), user.getImage());
    }



}
