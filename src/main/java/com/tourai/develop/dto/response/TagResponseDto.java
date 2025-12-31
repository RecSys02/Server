package com.tourai.develop.dto.response;

import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.enumType.TagType;

public record TagResponseDto(
        Long id,
        TagType tagType,
        String name
) {
    public static TagResponseDto from(Tag tag) {
        return new TagResponseDto(
                tag.getId(),
                tag.getTagType(),
                tag.getName()
        );
    }
}
