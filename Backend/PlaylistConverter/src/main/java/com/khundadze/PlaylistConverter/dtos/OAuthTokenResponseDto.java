package com.khundadze.PlaylistConverter.dtos;

import com.khundadze.PlaylistConverter.enums.MusicService;

public record OAuthTokenResponseDto(
        String accessToken,
        MusicService service) {

}
