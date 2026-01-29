package com.tourai.develop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.enumType.TagType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagDataSyncService {

    private final TagService tagService;
    private final ObjectMapper objectMapper;

    /**
     * JSON 파일을 읽어서 Tag 데이터를 DB와 동기화합니다.
     * - JSON에 있고 DB에 없으면: 추가
     * - JSON에 없고 DB에 있으면: 삭제하지 않고 로그로 남김 (추후 처리)
     * - 둘 다 있으면: 패스
     * @param filePath 리소스 경로
     */
    @Transactional
    public void syncTagsFromJson(String filePath) {
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            List<Tag> newTags = new ArrayList<>();

            rootNode.fields().forEachRemaining(field -> {
                String tagTypeName = field.getKey();
                JsonNode tagValues = field.getValue();

                try {
                    TagType tagType = TagType.valueOf(tagTypeName.toUpperCase());
                    if (tagValues.isArray()) {
                        StreamSupport.stream(tagValues.spliterator(), false)
                                .map(JsonNode::asText)
                                .map(tagName -> Tag.builder().tagType(tagType).name(tagName).build())
                                .forEach(newTags::add);
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown TagType: {}", tagTypeName);
                }
            });

            List<Tag> currentTags = tagService.findAll();

            // A record for cleaner comparison logic.
            record TagIdentifier(TagType tagType, String name) {
                static TagIdentifier from(Tag tag) {
                    return new TagIdentifier(tag.getTagType(), tag.getName());
                }
            }

            Map<TagIdentifier, Tag> currentTagMap = currentTags.stream()
                    .collect(Collectors.toMap(TagIdentifier::from, tag -> tag));
            Map<TagIdentifier, Tag> newTagMap = newTags.stream()
                    .collect(Collectors.toMap(TagIdentifier::from, tag -> tag, (t1, t2) -> {
                        log.warn("Duplicate tag found in JSON: type={}, name={}", t1.getTagType(), t1.getName());
                        return t1;
                    }));

            if (currentTagMap.keySet().equals(newTagMap.keySet())) {
                log.info("Tags are already up-to-date. Skipping synchronization.");
                return;
            }

            // 1. JSON에 없고 DB에 있는 태그 (삭제 대상이지만 삭제하지 않음)
            List<Tag> tagsNotInJson = currentTagMap.entrySet().stream()
                    .filter(entry -> !newTagMap.containsKey(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

            if (!tagsNotInJson.isEmpty()) {
                List<String> tagNames = tagsNotInJson.stream()
                        .map(tag -> tag.getTagType() + ":" + tag.getName())
                        .collect(Collectors.toList());
                log.info("Found {} tags in DB that are missing from JSON. Deletion skipped for now: {}", 
                        tagsNotInJson.size(), tagNames);
            }

            // 2. JSON에 있고 DB에 없는 태그 (추가 대상)
            List<Tag> tagsToAdd = newTagMap.entrySet().stream()
                    .filter(entry -> !currentTagMap.containsKey(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

            if (!tagsToAdd.isEmpty()) {
                tagService.saveAll(tagsToAdd);
                log.info("Added {} new tags.", tagsToAdd.size());
            }

            log.info("Tag synchronization completed for {}.", filePath);

        } catch (IOException e) {
            log.error("Failed to read Tag JSON file from {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Tag data synchronization failed", e);
        }
    }
}
