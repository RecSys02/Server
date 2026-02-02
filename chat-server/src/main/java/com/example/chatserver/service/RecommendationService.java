package com.example.chatserver.service;

import com.example.chatserver.client.CoreClient;
import com.example.chatserver.dto.*;
import com.example.chatserver.dto.request.AiRecommendationRequest;
import com.example.chatserver.dto.request.RecommendationRequestDto;
import com.example.chatserver.dto.response.AiRecommendationResponse;
import com.example.chatserver.dto.response.RecommendationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final WebClient fastApiWebClient;
    private final CoreClient coreClient;
    private final UserContextService userContextService;

    public RecommendationService(@Qualifier("fastApiWebClient") WebClient fastApiWebClient,
                                 CoreClient coreClient,
                                 UserContextService userContextService) {
        this.fastApiWebClient = fastApiWebClient;
        this.coreClient = coreClient;
        this.userContextService = userContextService;
    }

    public Mono<RecommendationResponseDto> recommend(Long userId, RecommendationRequestDto requestDto) {
        return userContextService.getOrFetch(userId)
                .flatMap(userContext -> {
                    AiRecommendationRequest aiRequest = createAiRequest(userId, userContext, requestDto);
                    return requestRecommendationToAi(aiRequest);
                })
                .flatMap(this::processAiResponse);
    }

    private Mono<AiRecommendationResponse> requestRecommendationToAi(AiRecommendationRequest request) {
        return fastApiWebClient
                .post()
                .uri("/recommend?top_k_per_category=10")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiRecommendationResponse.class);
    }

    private AiRecommendationRequest createAiRequest(Long userId, UserContextDto userContext, RecommendationRequestDto requestDto) {
        return new AiRecommendationRequest(
                userId,
                userContext.preferredThemes(),
                userContext.preferredMoods(),
                userContext.preferredRestaurantTypes(),
                userContext.preferredCafeTypes(),
                userContext.avoid(),
                userContext.activityLevel(),
                requestDto.region(),
                requestDto.accomAddress(),
                requestDto.companion(),
                requestDto.budget(),
                toAiSelectedPlaces(requestDto.historyPlaces()),
                toAiSelectedPlaces(requestDto.selectedPlaces())
        );
    }

    private Mono<RecommendationResponseDto> processAiResponse(AiRecommendationResponse aiResponse) {
        if (aiResponse == null || aiResponse.recommendations() == null) {
            return Mono.just(new RecommendationResponseDto(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        }

        List<Long> allPlaceIds = aiResponse.recommendations().stream()
                .filter(r -> r.items() != null)
                .flatMap(r -> r.items().stream())
                .map(AiRecommendationResponse.RecommendedItem::placeId)
                .distinct()
                .toList();

        if (allPlaceIds.isEmpty()) {
            return Mono.just(new RecommendationResponseDto(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        }

        return coreClient.getPlacesBulk(allPlaceIds)
                .map(places -> {
                    Map<Long, PlaceResponseDto> placeMap = places.stream()
                            .collect(Collectors.toMap(PlaceResponseDto::placeId, p -> p, (p1, p2) -> p1));

                    List<PlaceResponseDto> tourspots = new ArrayList<>();
                    List<PlaceResponseDto> restaurants = new ArrayList<>();
                    List<PlaceResponseDto> cafes = new ArrayList<>();

                    for (AiRecommendationResponse.CategoryAndRecommendedItems recommendation : aiResponse.recommendations()) {
                        if (recommendation.items() == null) continue;
                        String category = recommendation.category();

                        for (AiRecommendationResponse.RecommendedItem item : recommendation.items()) {
                            PlaceResponseDto place = placeMap.get(item.placeId());
                            if (place == null) continue;

                            if ("TOURSPOT".equalsIgnoreCase(category)) {
                                tourspots.add(place);
                            } else if ("RESTAURANT".equalsIgnoreCase(category)) {
                                restaurants.add(place);
                            } else if ("CAFE".equalsIgnoreCase(category)) {
                                cafes.add(place);
                            }
                        }
                    }
                    return new RecommendationResponseDto(tourspots, restaurants, cafes);
                });
    }

    private List<AiSelectedPlaceDto> toAiSelectedPlaces(List<SelectedPlaceDto> places) {
        if (places == null || places.isEmpty()) return Collections.emptyList();
        return places.stream()
                .filter(Objects::nonNull)
                .map(p -> new AiSelectedPlaceDto(p.placeId(), p.category(), p.province()))
                .toList();
    }
}
