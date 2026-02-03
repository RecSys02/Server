package com.example.chatserver.client;

import com.example.chatserver.dto.PlaceResponseDto;
import com.example.chatserver.dto.SelectedPlaceDto;
import com.example.chatserver.dto.UserContextDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoreClient {

    private final WebClient coreWebClient;

    public Mono<UserContextDto> fetchUserContext(Long userId) {
        return coreWebClient.get()
                .uri("/internal/users/context/{userId}", userId)
                .retrieve()
                .bodyToMono(UserContextDto.class);
    }

    public Mono<List<PlaceResponseDto>> getPlacesBulk(List<SelectedPlaceDto> selectedPlaces) {
        return coreWebClient.post()
                .uri("/api/places/bulk")
                .bodyValue(selectedPlaces)
                .retrieve()
                .bodyToFlux(PlaceResponseDto.class)
                .collectList();
    }
}
