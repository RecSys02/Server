package com.tourai.develop.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public record EditUserTagsDto (
         List<Long> updateTagIds // 새롭게 덮어씌울 태그 리스트
){}
