package com.tourai.develop.domain.entity;

import com.tourai.develop.domain.enumType.Category;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="tag_id")
    private Long id;

    @Column(nullable = false)
    private int tag_info;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false, length = 50)
    private String name;


}
