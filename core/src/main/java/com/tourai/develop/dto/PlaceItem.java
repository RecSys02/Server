package com.tourai.develop.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tourai.develop.domain.enumType.Category;
import com.tourai.develop.domain.enumType.Province;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PlaceItem {
    private String name;
    private Long placeId;
    private Category category;
    private Province province;
    private String startTime;
    private String endTime;
}
