package com.tourai.develop.service;

import com.tourai.develop.client.FastApiClient;
import com.tourai.develop.domain.entity.Place;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.entity.UserTag;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.TagType;
import com.tourai.develop.dto.AiSelectedPlaceDto;
import com.tourai.develop.dto.SelectedPlaceDto;
import com.tourai.develop.dto.request.RecommendationRequestDto;
import com.tourai.develop.dto.request.AiRecommendationRequest;
import com.tourai.develop.dto.response.AiRecommendationResponse;
import com.tourai.develop.dto.response.RecommendationResponseDto;
import com.tourai.develop.repository.PlaceRepository;
import com.tourai.develop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final FastApiClient fastApiClient;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    public RecommendationResponseDto recommend(Long userId, RecommendationRequestDto recommendationRequestDto) {

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
        List<SelectedPlaceDto> historyPlaces =
                (recommendationRequestDto.historyPlaces() == null) ? Collections.emptyList() : recommendationRequestDto.historyPlaces();
        List<SelectedPlaceDto> selectedPlaces =
                (recommendationRequestDto.selectedPlaces() == null) ? Collections.emptyList() : recommendationRequestDto.selectedPlaces();
        List<AiSelectedPlaceDto> aiHistoryPlaces = toAiSelectedPlaces(historyPlaces);
        List<AiSelectedPlaceDto> aiSelectedPlaces = toAiSelectedPlaces(selectedPlaces);


        AiRecommendationRequest aiRecommendationRequest = new AiRecommendationRequest(
                userId,
                preferredThemes,
                preferredMoods,
                preferredRestaurantTypes,
                preferredCafeTypes,
                avoid,
                activityLevel,
                recommendationRequestDto.region(),
                recommendationRequestDto.companion(),
                recommendationRequestDto.budget(),
                aiHistoryPlaces,
                aiSelectedPlaces
        );
        AiRecommendationResponse aiRecommendationResponse = fastApiClient.requestRecommendation(aiRecommendationRequest);
        return convertToFrontResponse(aiRecommendationResponse);
    }


    private String insertUserTagData(List<String> preferredThemes, List<String> preferredMoods, List<String> preferredRestaurantTypes, List<String> preferredCafeTypes, List<String> avoid, String activityLevel, List<UserTag> userTags) {
        if (userTags != null) {
            for (UserTag userTag : userTags) {
                TagType findTagType = userTag.getTag().getTagType();
                String findTagName = userTag.getTag().getName();
                switch (findTagType) {
                    case THEME -> preferredThemes.add(findTagName);

                    case MOOD -> preferredMoods.add(findTagName);

                    case ACTIVITY_LEVEL -> activityLevel = findTagName;

                    case RESTAURANT -> preferredRestaurantTypes.add(findTagName);

                    case CAFE -> preferredCafeTypes.add(findTagName);

                    case AVOID -> avoid.add(findTagName);
                }
            }
        }
        return activityLevel;
    }


    private RecommendationResponseDto convertToFrontResponse(AiRecommendationResponse aiResponse) {

        List<RecommendationResponseDto.RecommendedPlaceDto> tourspot = new ArrayList<>();
        List<RecommendationResponseDto.RecommendedPlaceDto> restaurants = new ArrayList<>();
        List<RecommendationResponseDto.RecommendedPlaceDto> cafes = new ArrayList<>();

        if (aiResponse == null || aiResponse.recommendations() == null) {
            return new RecommendationResponseDto(tourspot, restaurants, cafes);
        }

        for (AiRecommendationResponse.CategoryAndRecommendedItems recommendation : aiResponse.recommendations()) {
            Category recommendationCategory = recommendation.category();
            if (recommendation.items() == null) continue;
            for (AiRecommendationResponse.RecommendedItem item : recommendation.items()) {
                Place findPlace = placeRepository.findByPlaceIdAndCategoryAndProvince(item.placeId(), item.category(), item.province()).orElse(null);
                if (findPlace == null) continue;
                switch (recommendationCategory) {

                    case TOURSPOT -> {
                        RecommendationResponseDto.RecommendedPlaceDto recommendedPlaceDto = getRecommendedPlaceDto(findPlace);
                        tourspot.add(recommendedPlaceDto);
                    }
                    case RESTAURANT -> {
                        RecommendationResponseDto.RecommendedPlaceDto recommendedPlaceDto = getRecommendedPlaceDto(findPlace);
                        restaurants.add(recommendedPlaceDto);
                    }
                    case CAFE -> {
                        RecommendationResponseDto.RecommendedPlaceDto recommendedPlaceDto = getRecommendedPlaceDto(findPlace);
                        cafes.add(recommendedPlaceDto);
                    }

                }
            }
        }

        return new RecommendationResponseDto(tourspot, restaurants, cafes);
    }

    private RecommendationResponseDto.RecommendedPlaceDto getRecommendedPlaceDto(Place findPlace) {
        RecommendationResponseDto.RecommendedPlaceDto recommendedPlaceDto = new RecommendationResponseDto.RecommendedPlaceDto(
                findPlace.getId(), findPlace.getPlaceId(), findPlace.getName(), findPlace.getLatitude(),
                findPlace.getLongitude(), findPlace.getAddress(), findPlace.getDescription(),
                findPlace.getDuration(), findPlace.getImages(), findPlace.getKeywords(),
                findPlace.getCategory(), findPlace.getProvince()
        );
        return recommendedPlaceDto;
    }

    private List<AiSelectedPlaceDto> toAiSelectedPlaces(List<SelectedPlaceDto> places) {
        if (places == null || places.isEmpty()) return Collections.emptyList();
        return places.stream()
                .filter(Objects::nonNull)
                .map(p -> new AiSelectedPlaceDto(p.placeId(), p.category(), p.province()))
                .toList();
    }
}
