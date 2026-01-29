package com.tourai.develop.dto;

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
public class PlaceItem {
    private String name;
    private Long placeId;
    private Category category;
    private Province province;
    private String startTime;
    private String endTime;
}
