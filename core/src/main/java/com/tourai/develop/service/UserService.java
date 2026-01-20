package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.entity.UserTag;
import com.tourai.develop.domain.enumType.TagType;
import com.tourai.develop.dto.*;
import com.tourai.develop.exception.BusinessException;
import com.tourai.develop.exception.enumType.ErrorCode;
import com.tourai.develop.kafka.publisher.UserContextEventPublisher;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import com.tourai.develop.validation.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    // 회원정보 관리 메서드 구현
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final PasswordValidator passwordValidator;
    private final TagService tagService;
    private final UserContextEventPublisher userContextEventPublisher;

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
    public void editUserTags(Long userId, EditUserTagsDto editUserTagsDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.clearTags();

        attachTagsByTagNames(user, TagType.THEME, editUserTagsDto.preferredThemes());
        attachTagsByTagNames(user, TagType.MOOD, editUserTagsDto.preferredMoods());
        attachTagsByTagNames(user, TagType.RESTAURANT, editUserTagsDto.preferredRestaurantTypes());
        attachTagsByTagNames(user, TagType.CAFE, editUserTagsDto.preferredCafeTypes());
        attachTagsByTagNames(user, TagType.AVOID, editUserTagsDto.avoid());

        attachTagByTagName(user, TagType.ACTIVITY_LEVEL, editUserTagsDto.activityLevel());


        UserContextDto userContextDto = new UserContextDto(
                editUserTagsDto.preferredThemes(),
                editUserTagsDto.preferredMoods(),
                editUserTagsDto.preferredRestaurantTypes(),
                editUserTagsDto.preferredCafeTypes(),
                editUserTagsDto.avoid(),
                editUserTagsDto.activityLevel()
        );

        userContextEventPublisher.publish(userId, userContextDto);
    }

    public UserMeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new UserMeResponse(user.getUserName(), user.getImage());
    }


    public UserContextDto getUserContext(Long userId) {
        User user = userRepository.findByIdWithTags(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Tag> tags = user.getUserTags().stream()
                .map(UserTag::getTag)
                .filter(Objects::nonNull)
                .toList();

        List<String> preferredThemes = extract(tags, TagType.THEME);
        List<String> preferredMoods = extract(tags, TagType.MOOD);
        List<String> preferredRestaurantTypes = extract(tags, TagType.RESTAURANT);
        List<String> preferredCafeTypes = extract(tags, TagType.CAFE);
        List<String> avoid = extract(tags, TagType.AVOID);

        String activityLevel = extractSingle(tags, TagType.ACTIVITY_LEVEL);

        return new UserContextDto(
                preferredThemes,
                preferredMoods,
                preferredRestaurantTypes,
                preferredCafeTypes,
                avoid,
                activityLevel
        );
    }

    private List<String> extract(List<Tag> tags, TagType type) {
        return tags.stream()
                .filter(t -> t.getTagType() == type)
                .map(Tag::getName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String extractSingle(List<Tag> tags, TagType type) {
        return tags.stream()
                .filter(t -> t.getTagType() == type)
                .map(Tag::getName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }


    private void attachTagsByTagNames(User user, TagType tagType, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;

        List<String> distinctTagNames = tagNames.stream()
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        if (distinctTagNames.isEmpty()) return;

        List<Tag> findTags = tagService.getTagsByTagTypeAndNames(tagType, distinctTagNames);

        // 요청한 태그 이름 중 db에 없는게 한개로 있으면 실패
        if (findTags.size() != distinctTagNames.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        findTags.forEach(user::addTag);
    }

    private void attachTagByTagName(User user, TagType tagType, String tagName) {
        if (tagName == null || tagName.isBlank()) return;
        attachTagsByTagNames(user, tagType, List.of(tagName));
    }


}
