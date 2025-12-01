package com.tourai.develop.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "plan")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PlanTag> planTags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule", columnDefinition = "jsonb")
    @Builder.Default
    private Map<Integer, Map<String, String>> schedule = new HashMap<>();


    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_private")
    private boolean isPrivate;

    @Column(name = "like_count")
    @Builder.Default
    private Long like = 0L;

    public void updatePrivateStatus(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}