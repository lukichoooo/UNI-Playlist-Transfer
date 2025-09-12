package com.khundadze.PlaylistConverter.dtos;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;

public record ConvertPlaylistRequest(
        StreamingPlatform fromPlatform,
        StreamingPlatform toPlatform,
        Long fromPlaylistId,
        Long toPlaylistId) {

}
