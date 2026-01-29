package com.tourai.develop.dto.response;

import java.util.List;

public record UserInfoResponseDto(
        String email,
        String userName,
        String image,
        List<Long> tagIds
) {

}
