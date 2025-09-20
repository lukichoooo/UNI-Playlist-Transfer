package com.khundadze.PlaylistConverter.dtos;

import com.khundadze.PlaylistConverter.enums.StreamingPlatform;

public record ConvertPlaylistRequest(
        String transferState,
        StreamingPlatform fromPlatform,
        StreamingPlatform toPlatform,
        String fromPlaylistId,
        String newPlaylistName) {

}
