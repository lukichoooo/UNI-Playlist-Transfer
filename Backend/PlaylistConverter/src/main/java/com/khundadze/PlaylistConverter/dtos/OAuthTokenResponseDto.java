package com.khundadze.PlaylistConverter.dtos;

import com.khundadze.enums.MusicService;

public record OAuthTokenResponseDto(
        String accessToken,
        MusicService service) {

}
