package com.khundadze.PlaylistConverter.dtos;

import java.util.List;

public record TargetMusicDto(
        String id,
        String name,
        String artist,
        String album,
        List<String> keywordsLowList
) {
}