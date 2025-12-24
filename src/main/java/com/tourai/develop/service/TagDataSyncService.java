package com.tourai.develop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.enumType.TagType;
import com.tourai.develop.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagDataSyncService {

    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;

    /**
     * JSON 파일을 읽어서 Tag 데이터를 DB와 동기화합니다.
     * 기존 데이터와 비교하여 변경사항이 있을 때만 업데이트합니다.
     * @param filePath 리소스 경로
     */
    @Transactional
    public void syncTagsFromJson(String filePath) {
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            List<Tag> newTags = new ArrayList<>();

            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String tagTypeName = field.getKey();
                JsonNode tagValues = field.getValue();

                try {
                    TagType tagType = TagType.valueOf(tagTypeName);
                    if (tagValues.isArray()) {
                        for (JsonNode valueNode : tagValues) {
                            String tagName = valueNode.asText();
                            newTags.add(Tag.builder()
                                    .tagType(tagType)
                                    .name(tagName)
                                    .build());
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown TagType: {}", tagTypeName);
                }
            }

            List<Tag> currentTags = tagRepository.findAll();

            if (isTagsEqual(newTags, currentTags)) {
                log.info("Tags are already up-to-date. Skipping synchronization.");
                return;
            }

            if (!newTags.isEmpty()) {
                // 주의: deleteAll()은 외래키 제약 조건이 있을 경우 실패할 수 있습니다.
                // 운영 환경에서는 변경된 태그만 선별하여 업데이트하는 방식(Upsert)을 권장합니다.
                tagRepository.deleteAll();
                tagRepository.saveAll(newTags);
                log.info("Successfully synchronized {} tags from {}", newTags.size(), filePath);
            }

        } catch (IOException e) {
            log.error("Failed to read Tag JSON file from {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Tag data synchronization failed", e);
        }
    }

    private boolean isTagsEqual(List<Tag> newTags, List<Tag> currentTags) {
        if (newTags.size() != currentTags.size()) {
            return false;
        }

        Set<String> newTagSet = newTags.stream()
                .map(tag -> tag.getTagType() + ":" + tag.getName())
                .collect(Collectors.toSet());

        Set<String> currentTagSet = currentTags.stream()
                .map(tag -> tag.getTagType() + ":" + tag.getName())
                .collect(Collectors.toSet());

        return newTagSet.equals(currentTagSet);
    }
}
