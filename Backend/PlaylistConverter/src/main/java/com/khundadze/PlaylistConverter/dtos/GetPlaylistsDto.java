package com.khundadze.PlaylistConverter.dtos;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;

/**
 * DTO for requesting playlists from a specific music service.
 */
public record GetPlaylistsDto(
                StreamingPlatform service,
                String accessToken) {
}
