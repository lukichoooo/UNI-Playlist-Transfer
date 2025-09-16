package com.khundadze.PlaylistConverter.dtos;

public record PlaylistSearchDto(
        String id,
        String name,
        Integer totalTracks
) {
}
