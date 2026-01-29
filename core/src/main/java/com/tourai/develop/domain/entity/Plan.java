package com.tourai.develop.domain.entity;

import com.tourai.develop.dto.DailySchedule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private List<DailySchedule> schedule = new ArrayList<>();


    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "province")
    private String province;

    @Column(name = "is_private")
    private Boolean isPrivate;

    @Column(name = "image")
    private String image;

    @Formula("(select count(*) from plan_like pl where pl.plan_id = id)")
    private int likeCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public void updatePrivateStatus(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void addPlanTag(PlanTag planTag) {
        this.planTags.add(planTag);
    }
}