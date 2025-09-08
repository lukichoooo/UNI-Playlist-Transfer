package com.khundadze.PlaylistConverter.dtos;

import com.khundadze.PlaylistConverter.enums.MusicService;
import java.util.List;

/**
 * DTO for creating a playlist on a music service.
 */
public record CreatePlaylistDto(
        MusicService service,
        String playlistName,
        List<String> tracks,
        String accessToken) {
}
