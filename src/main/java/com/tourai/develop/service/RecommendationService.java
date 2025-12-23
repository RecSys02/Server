package com.tourai.develop.service;

import com.tourai.develop.client.FastApiClient;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.entity.UserTag;
import com.tourai.develop.domain.enumType.TagType;
import com.tourai.develop.dto.RecommendationDto;
import com.tourai.develop.dto.request.AiRecommendationRequest;
import com.tourai.develop.dto.response.AiRecommendationResponse;
import com.tourai.develop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final FastApiClient fastApiClient;
    private final UserRepository userRepository;

    public AiRecommendationResponse recommend(Long userId, RecommendationDto recommendationDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id 의 user 가 존재하지 않습니다!, userId : " + userId));


        List<String> preferredThemes = new ArrayList<>();
        List<String> preferredMoods = new ArrayList<>();
        List<String> preferredRestaurantTypes = new ArrayList<>();
        List<String> preferredCafeTypes = new ArrayList<>();
        List<String> avoid = new ArrayList<>();
        String activityLevel = null;

        List<UserTag> userTags = user.getUserTags();

        activityLevel = insertUserTagData(preferredThemes, preferredMoods, preferredRestaurantTypes, preferredCafeTypes, avoid, activityLevel, userTags);

        AiRecommendationRequest aiRecommendationRequest = new AiRecommendationRequest(userId, preferredThemes, preferredMoods, preferredRestaurantTypes, preferredCafeTypes,
                avoid, activityLevel, recommendationDto.city(),
                recommendationDto.companionType(),
                recommendationDto.budget(),
                recommendationDto.visitCafe(),
                recommendationDto.visitRestaurant(),
                recommendationDto.visitTourspot(),
                recommendationDto.lastSelectedPoi());

        return fastApiClient.requestRecommendation(aiRecommendationRequest);
    }


    private String insertUserTagData(List<String> preferredThemes, List<String> preferredMoods, List<String> preferredRestaurantTypes, List<String> preferredCafeTypes, List<String> avoid, String activityLevel, List<UserTag> userTags) {
        if (userTags != null) {
            for (UserTag userTag : userTags) {
                TagType findTagType = userTag.getTag().getTagType();
                String findTagName = userTag.getTag().getName();
                switch (findTagType) {
                    case THEME:
                        preferredThemes.add(findTagName);
                        break;
                    case MOOD:
                        preferredMoods.add(findTagName);
                        break;

                    case ACTIVITY_LEVEL:
                        activityLevel = findTagName;
                        break;

                    case RESTAURANT:
                        preferredRestaurantTypes.add(findTagName);
                        break;

                    case CAFE:
                        preferredCafeTypes.add(findTagName);
                        break;

                    case AVOID:
                        avoid.add(findTagName);
                        break;
                    default:
                        break;
                }
            }
        }
        return activityLevel;
    }
}
