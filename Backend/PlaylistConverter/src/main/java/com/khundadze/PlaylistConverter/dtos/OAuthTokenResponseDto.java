package com.khundadze.PlaylistConverter.dtos;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;

public record OAuthTokenResponseDto(
        String accessToken,
        StreamingPlatform service) {

}
