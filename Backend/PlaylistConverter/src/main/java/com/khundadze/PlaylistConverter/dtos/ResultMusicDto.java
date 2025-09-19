package com.khundadze.PlaylistConverter.dtos;

import java.util.HashSet;

public record ResultMusicDto(
        String id,
        String name,
        String artist,
        String album,
        String isrc,
        String duration,

        HashSet<String> keywordsLowSet
) {
}