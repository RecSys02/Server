package com.tourai.develop.repository;

import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.enumType.TagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByTagTypeAndNameIn(TagType tagType, List<String> names);
}
