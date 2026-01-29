package com.tourai.develop.domain.entity;

import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.TagType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tag", uniqueConstraints = {
    @UniqueConstraint(name = "uk_tag_type_name", columnNames = {"tagType", "name"})
})
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TagType tagType;

    @Column(nullable = false, length = 50)
    private String name;


}
