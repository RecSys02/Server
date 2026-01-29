package com.tourai.develop.controller;

import com.tourai.develop.dto.response.TagResponseDto;
import com.tourai.develop.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagResponseDto>> getAllTags() {
        return ResponseEntity.ok(tagService.findAllTags());
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<TagResponseDto> getTag(@PathVariable("tagId") Long tagId) {
        return ResponseEntity.ok(tagService.findTagById(tagId));
    }
}
