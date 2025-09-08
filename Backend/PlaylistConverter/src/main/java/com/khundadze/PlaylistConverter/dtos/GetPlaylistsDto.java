package com.khundadze.PlaylistConverter.dtos;

import com.khundadze.PlaylistConverter.enums.MusicService;

/**
 * DTO for requesting playlists from a specific music service.
 */
public record GetPlaylistsDto(
        MusicService service,
        String accessToken) {
}
