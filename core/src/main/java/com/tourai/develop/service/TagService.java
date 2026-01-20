package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.enumType.TagType;
import com.tourai.develop.dto.response.TagResponseDto;
import com.tourai.develop.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TagResponseDto> findAllTags() {
        return tagRepository.findAll().stream()
                .map(TagResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TagResponseDto findTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));
        return TagResponseDto.from(tag);
    }

    @Transactional
    public void saveAll(List<Tag> tags) {
        tagRepository.saveAll(tags);
    }


    @Transactional(readOnly = true)
    public List<Tag> getTagsByTagTypeAndNames(TagType tagType, List<String> names) {
        if (names == null || names.isEmpty()) return List.of();
        return tagRepository.findByTagTypeAndNameIn(tagType, names);
    }

}
