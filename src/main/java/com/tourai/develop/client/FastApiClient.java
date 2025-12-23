package com.tourai.develop.client;

import com.tourai.develop.dto.request.AiRecommendationRequest;
import com.tourai.develop.dto.response.AiRecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class FastApiClient {


    private final WebClient webClient;
    private final String aiBaseUrl;

    public FastApiClient(WebClient webClient, @Value("${ai.base-url}") String aiBaseUrl) {
        this.webClient = webClient;
        this.aiBaseUrl = aiBaseUrl;
    }

    public AiRecommendationResponse requestRecommendation(AiRecommendationRequest request) {
        return webClient
                .post()
                .uri(aiBaseUrl + "/recommend?top_k_per_category=10")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiRecommendationResponse.class)
                .block();
    }


}
