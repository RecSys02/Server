package com.tourai.develop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Region;
import com.tourai.develop.dto.PlaceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceDataSyncService {

    private final PlaceService placeService;
    private final ObjectMapper objectMapper;

    /**
     * JSON 파일을 읽어서 DB와 동기화합니다.
     * @param filePath 리소스 경로 (예: "data/place/poi_seoul.json")
     */
    public void syncFromJson(String filePath) {
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(inputStream);

            List<PlaceInfo> placeInfos = new ArrayList<>();

            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    try {
                        placeInfos.add(mapToPlaceInfo(node));
                    } catch (Exception e) {
                        log.error("Failed to map JSON node to PlaceInfo: {}", node, e);
                    }
                }
            }

            placeService.syncPlaces(placeInfos);
            log.info("Successfully synchronized {} places from {}", placeInfos.size(), filePath);
        } catch (IOException e) {
            log.error("Failed to read JSON file from {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Data synchronization failed", e);
        }
    }

    private PlaceInfo mapToPlaceInfo(JsonNode node) {
        Long placeId = node.get("place_id").asLong();

        String name = node.get("name").asText();
        String address = node.has("address") ? node.get("address").asText() : "";
        String description = node.has("description") ? node.get("description").asText() : "";

        List<String> images = new ArrayList<>();
        JsonNode imagesNode = node.path("images");
        if (imagesNode.isArray()) {
            for (JsonNode imgNode : imagesNode) {
                String imageUrl = imgNode.asText();
                if (imageUrl != null && (imageUrl.startsWith("http"))) {
                    images.add(imageUrl);
                }
            }
        }

        List<String> keywords = new ArrayList<>();
        JsonNode keywordsNode = node.path("keywords");
        if (keywordsNode.isArray()) {
            for (JsonNode kwNode : keywordsNode) {
                keywords.add(kwNode.asText());
            }
        }

        Double latitude = node.get("latitude").asDouble();
        Double longitude = node.get("longitude").asDouble();

        String categoryStr = node.get("category").asText().toUpperCase();
        Category category = Category.valueOf(categoryStr);

        JsonNode durationNode = node.path("duration");
        String duration = null;
        if (!durationNode.isMissingNode() && !durationNode.isNull()) {
            duration = durationNode.asText();
        }

        Region region = Region.valueOf(node.get("region").asText().toUpperCase());

        return new PlaceInfo(
                placeId,
                category,
                region,
                name,
                address,
                duration,
                description,
                images,
                keywords,
                latitude,
                longitude
        );
    }
}
