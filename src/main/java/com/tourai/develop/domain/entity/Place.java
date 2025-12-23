package com.tourai.develop.domain.entity;

import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Region;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "place")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_region", nullable = false)
    private Region placeRegion;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", nullable = false, length = 100)
    private String address;

    @Column(name = "duration", length = 50)
    private String duration;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    public void update(Category category, Region placeRegion, String name, String address, String duration, String description, List<String> images, Double latitude, Double longitude) {
        this.category = category;
        this.placeRegion = placeRegion;
        this.name = name;
        this.address = address;
        this.duration = duration;
        this.description = description;
        this.images = images;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}