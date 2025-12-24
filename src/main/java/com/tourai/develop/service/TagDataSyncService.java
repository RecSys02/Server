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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

            List<Tag> currentTags = tagRepository.findAll();

            // A record for cleaner comparison logic.
            record TagIdentifier(TagType tagType, String name) {
                static TagIdentifier from(Tag tag) {
                    return new TagIdentifier(tag.getTagType(), tag.getName());
                }
            }

            Map<TagIdentifier, Tag> currentTagMap = currentTags.stream()
                    .collect(Collectors.toMap(TagIdentifier::from, tag -> tag));
            Map<TagIdentifier, Tag> newTagMap = newTags.stream()
                    .collect(Collectors.toMap(TagIdentifier::from, tag -> tag, (t1, t2) -> t1));

            if (currentTagMap.keySet().equals(newTagMap.keySet())) {
                log.info("Tags are already up-to-date. Skipping synchronization.");
                return;
            }

            List<Tag> tagsToDelete = currentTagMap.entrySet().stream()
                    .filter(entry -> !newTagMap.containsKey(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

            List<Tag> tagsToAdd = newTagMap.entrySet().stream()
                    .filter(entry -> !currentTagMap.containsKey(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

            if (!tagsToDelete.isEmpty()) {
                int deletedCount = 0;
                for (Tag tag : tagsToDelete) {
                    try {
                        tagRepository.delete(tag);
                        deletedCount++;
                    } catch (Exception e) {
                        log.warn("Failed to delete tag '{}' (ID: {}). It might be in use. Error: {}",
                                tag.getName(), tag.getId(), e.getMessage());
                    }
                }
                if (deletedCount > 0) {
                    log.info("Deleted {} obsolete tags.", deletedCount);
                }
            }

            if (!tagsToAdd.isEmpty()) {
                tagRepository.saveAll(tagsToAdd);
                log.info("Added {} new tags.", tagsToAdd.size());
            }
            log.info("Tag synchronization completed for {}.", filePath);

        } catch (IOException e) {
            log.error("Failed to read Tag JSON file from {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Tag data synchronization failed", e);
        }
    }
}
